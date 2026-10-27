package com.axiel7.moelist.data.network

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.manga.JikanMangaStatisticsResponse

/** Jikan (unofficial MAL API). Calls go through a client without any MAL credentials. */
class JikanApi(private val service: JikanService) {

    /** Errors come back in the [Response]'s `error` / `message`, with the HTTP code. */
    suspend fun getUserStats(username: String): Response<UserStats> =
        service.getUserStats(username).bodyOrError()

    suspend fun getMangaStatistics(mangaId: Int): JikanMangaStatisticsResponse =
        service.getMangaStatistics(mangaId).bodyOrThrow()
}
