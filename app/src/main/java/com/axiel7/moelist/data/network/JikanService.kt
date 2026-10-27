package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.manga.JikanMangaStatisticsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response as HttpResponse

/**
 * https://api.jikan.moe/v4/ (unofficial, public). Uses a client without the MAL client id or
 * token, so nothing of the user's leaks to a third party.
 */
interface JikanService {

    @GET("users/{username}/statistics")
    suspend fun getUserStats(@Path("username") username: String): HttpResponse<Response<UserStats>>

    @GET("manga/{id}/statistics")
    suspend fun getMangaStatistics(@Path("id") mangaId: Int): HttpResponse<JikanMangaStatisticsResponse>
}
