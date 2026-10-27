package com.axiel7.moelist.data.network

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.anime.AnimeDetails
import com.axiel7.moelist.data.model.anime.AnimeList
import com.axiel7.moelist.data.model.anime.AnimeRanking
import com.axiel7.moelist.data.model.anime.AnimeSeasonal
import com.axiel7.moelist.data.model.anime.MyAnimeListStatus
import com.axiel7.moelist.data.model.anime.UserAnimeList
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.Character
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort

/**
 * The MyAnimeList API as the repositories use it. Same functions as the Ktor version, now on
 * Retrofit ([MalService] / [MalAuthService]):
 * - list calls return [Response] with `error` / `message` filled on HTTP errors (never throw
 *   for those, only for no connection),
 * - single objects return the body or throw [ApiException],
 * - [getUser] and [getAccessToken] put the error into the object's own fields, as before,
 * - delete calls return whether MAL removed the entry.
 */
class Api(
    private val service: MalService,
    private val authService: MalAuthService,
) {
    // Login

    suspend fun getAccessToken(
        clientId: String,
        code: String,
        codeVerifier: String,
        grantType: String,
        redirectUri: String,
    ): AccessToken = authService.getAccessToken(
        clientId = clientId,
        code = code,
        codeVerifier = codeVerifier,
        grantType = grantType,
        redirectUri = redirectUri,
    ).accessTokenOrError()

    suspend fun getAccessToken(
        clientId: String,
        refreshToken: String,
    ): AccessToken = authService.refreshAccessToken(
        clientId = clientId,
        refreshToken = refreshToken,
        grantType = "refresh_token",
    ).accessTokenOrError()

    private fun retrofit2.Response<AccessToken>.accessTokenOrError(): AccessToken =
        if (isSuccessful) {
            body() ?: AccessToken(message = "Empty response (HTTP ${code()})")
        } else {
            val exception = toApiException()
            AccessToken(error = exception.error, message = exception.message)
        }

    // Anime

    suspend fun getAnimeList(
        query: String,
        limit: Int,
        offset: Int?,
        nsfw: Int,
        fields: String?,
    ): Response<List<AnimeList>> =
        service.getAnimeList(query, limit, offset, nsfw, fields).bodyOrError()

    suspend fun getAnimeList(url: String): Response<List<AnimeList>> =
        service.getAnimeList(url).bodyOrError()

    suspend fun getSeasonalAnime(
        sort: MediaSort,
        year: Int,
        season: String,
        limit: Int,
        nsfw: Int,
        fields: String?,
    ): Response<List<AnimeSeasonal>> =
        service.getSeasonalAnime(year, season, sort.value, limit, nsfw, fields).bodyOrError()

    suspend fun getSeasonalAnime(url: String): Response<List<AnimeSeasonal>> =
        service.getSeasonalAnime(url).bodyOrError()

    suspend fun getAnimeRanking(
        rankingType: String,
        limit: Int,
        nsfw: Int,
        fields: String?,
    ): Response<List<AnimeRanking>> =
        service.getAnimeRanking(rankingType, limit, offset = 0, nsfw, fields).bodyOrError()

    suspend fun getAnimeRanking(url: String): Response<List<AnimeRanking>> =
        service.getAnimeRanking(url).bodyOrError()

    /** [status] `null` returns every status. */
    suspend fun getUserAnimeList(
        status: ListStatus?,
        sort: MediaSort?,
        limit: Int?,
        nsfw: Int,
        fields: String?,
    ): Response<List<UserAnimeList>> =
        service.getUserAnimeList(status?.value, sort?.value, limit, nsfw, fields).bodyOrError()

    suspend fun getUserAnimeList(url: String): Response<List<UserAnimeList>> =
        service.getUserAnimeList(url).bodyOrError()

    suspend fun updateUserAnimeList(
        animeId: Int,
        status: ListStatus?,
        @IntRange(0, 10) score: Int?,
        watchedEpisodes: Int?,
        startDate: String?,
        endDate: String?,
        isRewatching: Boolean?,
        numRewatches: Int?,
        @IntRange(0, 5) rewatchValue: Int?,
        @IntRange(0, 2) priority: Int?,
        tags: String?,
        comments: String?,
    ): MyAnimeListStatus = service.updateUserAnimeList(
        animeId = animeId,
        status = status?.value,
        score = score,
        watchedEpisodes = watchedEpisodes,
        startDate = startDate,
        endDate = endDate,
        isRewatching = isRewatching,
        numRewatches = numRewatches,
        rewatchValue = rewatchValue,
        priority = priority,
        tags = tags,
        comments = comments,
    ).bodyOrThrow()

    /** `true` if MAL removed the entry. */
    suspend fun deleteAnimeEntry(animeId: Int): Boolean =
        service.deleteAnimeEntry(animeId).isSuccessful

    suspend fun getAnimeDetails(
        animeId: Int,
        fields: String?,
    ): AnimeDetails = service.getAnimeDetails(animeId, fields).bodyOrThrow()

    suspend fun getAnimeCharacters(
        animeId: Int,
        limit: Int?,
        offset: Int?,
        fields: String?,
    ): Response<List<Character>> =
        service.getAnimeCharacters(animeId, limit, offset, fields).bodyOrError()

    suspend fun getAnimeCharacters(url: String): Response<List<Character>> =
        service.getAnimeCharacters(url).bodyOrError()

    // Manga

    suspend fun getMangaList(
        query: String,
        limit: Int,
        offset: Int?,
        nsfw: Int,
        fields: String?,
    ): Response<List<MangaList>> =
        service.getMangaList(query, limit, offset, nsfw, fields).bodyOrError()

    suspend fun getMangaList(url: String): Response<List<MangaList>> =
        service.getMangaList(url).bodyOrError()

    /** [status] `null` returns every status. */
    suspend fun getUserMangaList(
        status: ListStatus?,
        sort: MediaSort?,
        limit: Int?,
        nsfw: Int,
        fields: String?,
    ): Response<List<UserMangaList>> =
        service.getUserMangaList(status?.value, sort?.value, limit, nsfw, fields).bodyOrError()

    suspend fun getUserMangaList(url: String): Response<List<UserMangaList>> =
        service.getUserMangaList(url).bodyOrError()

    suspend fun getMangaRanking(
        rankingType: String,
        limit: Int,
        nsfw: Int,
        fields: String?,
    ): Response<List<MangaRanking>> =
        service.getMangaRanking(rankingType, limit, nsfw, fields).bodyOrError()

    suspend fun getMangaRanking(url: String): Response<List<MangaRanking>> =
        service.getMangaRanking(url).bodyOrError()

    suspend fun updateUserMangaList(
        mangaId: Int,
        status: ListStatus?,
        @IntRange(0, 10) score: Int?,
        chaptersRead: Int?,
        volumesRead: Int?,
        startDate: String?,
        endDate: String?,
        isRereading: Boolean?,
        numRereads: Int?,
        @IntRange(0, 5) rereadValue: Int?,
        @IntRange(0, 2) priority: Int?,
        tags: String?,
        comments: String?,
    ): MyMangaListStatus = service.updateUserMangaList(
        mangaId = mangaId,
        status = status?.value,
        score = score,
        chaptersRead = chaptersRead,
        volumesRead = volumesRead,
        startDate = startDate,
        endDate = endDate,
        isRereading = isRereading,
        numRereads = numRereads,
        rereadValue = rereadValue,
        priority = priority,
        tags = tags,
        comments = comments,
    ).bodyOrThrow()

    /** `true` if MAL removed the entry. */
    suspend fun deleteMangaEntry(mangaId: Int): Boolean =
        service.deleteMangaEntry(mangaId).isSuccessful

    suspend fun getMangaDetails(
        mangaId: Int,
        fields: String?,
    ): MangaDetails = service.getMangaDetails(mangaId, fields).bodyOrThrow()

    // User

    suspend fun getUser(fields: String?): User {
        val response = service.getUser(fields)
        return if (response.isSuccessful) {
            response.body() ?: User(message = "Empty response (HTTP ${response.code()})")
        } else {
            val exception = response.toApiException()
            User(error = exception.error, message = exception.message)
        }
    }
}
