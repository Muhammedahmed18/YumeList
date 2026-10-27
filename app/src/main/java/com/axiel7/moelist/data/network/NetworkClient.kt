package com.axiel7.moelist.data.network

import android.content.Context
import com.axiel7.moelist.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/*
 * The app's HTTP stack: one OkHttp core shared by everything (one connection pool, one
 * dispatcher, one disk cache), with small per-use variants made with newBuilder():
 *
 * - base:  cache + timeouts.
 * - auth:  base with its own dispatcher, for the OAuth token endpoint (a refresh runs while
 *          the failed MAL call still holds a dispatcher slot, so it must not wait for one).
 * - MAL:   base + MAL headers (client id, bearer) + token refresh on 401 + debug logging.
 * - Jikan: base + debug logging (no MAL headers, so no token is ever sent to Jikan).
 * - image: base without the HTTP cache (Coil keeps its own disk cache for pictures).
 */

/** Same leniency as the old Ktor client: MAL and Jikan change or omit fields now and then. */
val NetworkJson = Json {
    coerceInputValues = true
    isLenient = true
    ignoreUnknownKeys = true
}

private const val HTTP_CACHE_SIZE = 10L * 1024 * 1024 // 10 MB

fun createBaseOkHttpClient(context: Context): OkHttpClient =
    OkHttpClient.Builder()
        .cache(Cache(context.cacheDir.resolve("http_cache"), HTTP_CACHE_SIZE))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

fun createAuthOkHttpClient(base: OkHttpClient): OkHttpClient =
    base.newBuilder()
        .dispatcher(Dispatcher())
        .build()

fun createMalOkHttpClient(
    base: OkHttpClient,
    tokenStore: TokenStore,
    authService: MalAuthService,
): OkHttpClient =
    base.newBuilder()
        .addInterceptor(MalHeaderInterceptor(tokenStore))
        .authenticator(TokenAuthenticator(tokenStore, authService))
        .addDebugLogging()
        .build()

fun createJikanOkHttpClient(base: OkHttpClient): OkHttpClient =
    base.newBuilder()
        .addDebugLogging()
        .build()

fun createImageOkHttpClient(base: OkHttpClient): OkHttpClient =
    base.newBuilder()
        .cache(null)
        .build()

/** Full request/response logging in debug builds only, with the bearer token hidden. */
private fun OkHttpClient.Builder.addDebugLogging(): OkHttpClient.Builder = apply {
    if (BuildConfig.DEBUG) {
        addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
            }
        )
    }
}

fun createRetrofit(client: OkHttpClient, baseUrl: String): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
        .build()
