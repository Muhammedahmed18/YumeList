package com.axiel7.moelist.data.network

import com.axiel7.moelist.App
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val HEADER_AUTHORIZATION = "Authorization"
private const val HEADER_CLIENT_ID = "X-MAL-CLIENT-ID"

/**
 * Reads and saves the MAL tokens.
 *
 * The tokens live in DataStore, the single source of truth (login, logout and the refresh all
 * write there), so they're read fresh on every call: DataStore keeps the current values in
 * memory, so this is cheap. Interceptors and authenticators run on OkHttp's background threads,
 * which is why blocking here is fine.
 */
class TokenStore(private val preferences: DefaultPreferencesRepository) {

    val accessToken: String?
        get() = runBlocking { preferences.accessToken.first() }

    val refreshToken: String?
        get() = runBlocking { preferences.refreshToken.first() }

    fun save(token: AccessToken) {
        runBlocking { preferences.saveTokens(token) }
        token.accessToken?.let { App.accessToken = it }
    }
}

/** Adds the client id to every MAL call, and the bearer token when the user is logged in. */
class MalHeaderInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header(HEADER_CLIENT_ID, BuildConfig.CLIENT_ID)
        if (chain.request().header(HEADER_AUTHORIZATION) == null) {
            tokenStore.accessToken?.let { builder.header(HEADER_AUTHORIZATION, "Bearer $it") }
        }
        return chain.proceed(builder.build())
    }
}

/**
 * On a 401 from MAL, refreshes the access token once and retries the request with the new one.
 *
 * - Several calls can fail at the same time; the lock makes the first one refresh and the others
 *   just retry with the token it saved.
 * - If the refresh fails, the 401 goes through untouched, so the screens show "session expired"
 *   as before (the tokens are kept: a network blip shouldn't log anyone out).
 *
 * This replaces the Ktor refresh, which used a client without JSON support and so never
 * actually refreshed.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authService: MalAuthService,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only retry once, and only requests that were sent with a token
        if (response.priorResponse != null) return null
        val failedToken = response.request.header(HEADER_AUTHORIZATION)
            ?.removePrefix("Bearer ")
            ?: return null

        synchronized(lock) {
            val current = tokenStore.accessToken
            if (current != null && current != failedToken) {
                // Another call already refreshed while this one waited
                return response.request.withToken(current)
            }

            val refreshToken = tokenStore.refreshToken ?: return null
            val newToken = runBlocking {
                runCatching {
                    authService.refreshAccessToken(
                        clientId = BuildConfig.CLIENT_ID,
                        refreshToken = refreshToken,
                        grantType = "refresh_token",
                    ).body()
                }.getOrNull()
            }
            val newAccessToken = newToken?.accessToken ?: return null

            tokenStore.save(newToken)
            return response.request.withToken(newAccessToken)
        }
    }

    private fun Request.withToken(token: String) =
        newBuilder().header(HEADER_AUTHORIZATION, "Bearer $token").build()
}
