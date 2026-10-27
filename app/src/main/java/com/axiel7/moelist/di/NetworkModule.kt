package com.axiel7.moelist.di

import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.JikanApi
import com.axiel7.moelist.data.network.JikanService
import com.axiel7.moelist.data.network.MalAuthService
import com.axiel7.moelist.data.network.MalService
import com.axiel7.moelist.data.network.TokenStore
import com.axiel7.moelist.data.network.createAuthOkHttpClient
import com.axiel7.moelist.data.network.createBaseOkHttpClient
import com.axiel7.moelist.data.network.createImageOkHttpClient
import com.axiel7.moelist.data.network.createJikanOkHttpClient
import com.axiel7.moelist.data.network.createMalOkHttpClient
import com.axiel7.moelist.data.network.createRetrofit
import com.axiel7.moelist.utils.JIKAN_API_URL
import com.axiel7.moelist.utils.MAL_API_URL
import com.axiel7.moelist.utils.MAL_OAUTH2_URL
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val BASE_CLIENT = "base_client"
private const val AUTH_CLIENT = "auth_client"
private const val MAL_CLIENT = "mal_client"
private const val JIKAN_CLIENT = "jikan_client"

/** Qualifier of the OkHttp client Coil loads images with (see App.newImageLoader). */
const val IMAGE_CLIENT = "image_client"

val networkModule = module {
    // OkHttp: one shared core, variants per use (see NetworkClient.kt)
    single(named(BASE_CLIENT)) { createBaseOkHttpClient(androidContext()) }
    single(named(AUTH_CLIENT)) { createAuthOkHttpClient(get(named(BASE_CLIENT))) }
    single(named(JIKAN_CLIENT)) { createJikanOkHttpClient(get(named(BASE_CLIENT))) }
    single<OkHttpClient>(named(IMAGE_CLIENT)) { createImageOkHttpClient(get(named(BASE_CLIENT))) }
    single(named(MAL_CLIENT)) {
        createMalOkHttpClient(
            base = get(named(BASE_CLIENT)),
            tokenStore = get(),
            authService = get(),
        )
    }

    singleOf(::TokenStore)

    // Retrofit services
    single<MalAuthService> {
        createRetrofit(get(named(AUTH_CLIENT)), MAL_OAUTH2_URL).create(MalAuthService::class.java)
    }
    single<MalService> {
        createRetrofit(get(named(MAL_CLIENT)), MAL_API_URL).create(MalService::class.java)
    }
    single<JikanService> {
        createRetrofit(get(named(JIKAN_CLIENT)), JIKAN_API_URL).create(JikanService::class.java)
    }

    singleOf(::Api)
    singleOf(::JikanApi)
}
