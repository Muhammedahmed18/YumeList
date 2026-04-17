package com.axiel7.moelist.data.network

import com.axiel7.moelist.App
import com.axiel7.moelist.BuildConfig
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.utils.MAL_OAUTH2_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

fun createKtorHttpClient(
    preferencesRepository: DefaultPreferencesRepository
) = HttpClient(OkHttp) {

    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                coerceInputValues = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }

    install(HttpCache)

    if (BuildConfig.DEBUG) {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }
    }

    install(Auth) {
        bearer {
            loadTokens {
                val accessToken = preferencesRepository.accessToken.first()
                val refreshToken = preferencesRepository.refreshToken.first()
                if (accessToken != null && refreshToken != null) {
                    BearerTokens(accessToken, refreshToken)
                } else null
            }

            refreshTokens {
                val refreshToken = preferencesRepository.refreshToken.first() ?: return@refreshTokens null
                try {
                    // Use a separate client for refreshing to avoid infinite loops
                    val tokenResponse = HttpClient(OkHttp).submitForm(
                        url = "${MAL_OAUTH2_URL}token",
                        formParameters = parameters {
                            append("client_id", BuildConfig.CLIENT_ID)
                            append("grant_type", "refresh_token")
                            append("refresh_token", refreshToken)
                        }
                    ).body<AccessToken>()

                    if (tokenResponse.accessToken != null) {
                        preferencesRepository.saveTokens(tokenResponse)
                        App.accessToken = tokenResponse.accessToken
                        BearerTokens(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken ?: refreshToken
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            
            // Send token for all MAL API requests
            sendWithoutRequest { request ->
                request.url.host.contains("myanimelist.net")
            }
        }
    }

    install(DefaultRequest) {
        header("X-MAL-CLIENT-ID", BuildConfig.CLIENT_ID)
    }
}
