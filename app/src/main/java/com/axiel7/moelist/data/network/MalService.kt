package com.axiel7.moelist.data.network

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
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import retrofit2.Response as HttpResponse

/*
 * Retrofit definitions of the MyAnimeList endpoints the app uses.
 *
 * - Every call returns Retrofit's [HttpResponse] so [Api] can read error bodies
 *   (MAL answers errors with {"error": "...", "message": "..."}).
 * - Null query / form values are left out of the request, like the Ktor version did.
 * - The `@Url` variants follow MAL's absolute `paging.next` links.
 */

/** https://api.myanimelist.net/v2/ (client id + bearer token added by [MalHeaderInterceptor]). */
interface MalService {

    // Anime

    @GET("anime")
    suspend fun getAnimeList(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int?,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<AnimeList>>>

    @GET
    suspend fun getAnimeList(@Url url: String): HttpResponse<Response<List<AnimeList>>>

    @GET("anime/season/{year}/{season}")
    suspend fun getSeasonalAnime(
        @Path("year") year: Int,
        @Path("season") season: String,
        @Query("sort") sort: String,
        @Query("limit") limit: Int,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<AnimeSeasonal>>>

    @GET
    suspend fun getSeasonalAnime(@Url url: String): HttpResponse<Response<List<AnimeSeasonal>>>

    @GET("anime/ranking")
    suspend fun getAnimeRanking(
        @Query("ranking_type") rankingType: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<AnimeRanking>>>

    @GET
    suspend fun getAnimeRanking(@Url url: String): HttpResponse<Response<List<AnimeRanking>>>

    @GET("users/@me/animelist")
    suspend fun getUserAnimeList(
        @Query("status") status: String?,
        @Query("sort") sort: String?,
        @Query("limit") limit: Int?,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<UserAnimeList>>>

    @GET
    suspend fun getUserAnimeList(@Url url: String): HttpResponse<Response<List<UserAnimeList>>>

    @FormUrlEncoded
    @PATCH("anime/{id}/my_list_status")
    suspend fun updateUserAnimeList(
        @Path("id") animeId: Int,
        @Field("status") status: String?,
        @Field("score") score: Int?,
        @Field("num_watched_episodes") watchedEpisodes: Int?,
        @Field("start_date") startDate: String?,
        @Field("finish_date") endDate: String?,
        @Field("is_rewatching") isRewatching: Boolean?,
        @Field("num_times_rewatched") numRewatches: Int?,
        @Field("rewatch_value") rewatchValue: Int?,
        @Field("priority") priority: Int?,
        @Field("tags") tags: String?,
        @Field("comments") comments: String?,
    ): HttpResponse<MyAnimeListStatus>

    @DELETE("anime/{id}/my_list_status")
    suspend fun deleteAnimeEntry(@Path("id") animeId: Int): HttpResponse<Unit>

    @GET("anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") animeId: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<AnimeDetails>

    @GET("anime/{id}/characters")
    suspend fun getAnimeCharacters(
        @Path("id") animeId: Int,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<Character>>>

    @GET
    suspend fun getAnimeCharacters(@Url url: String): HttpResponse<Response<List<Character>>>

    // Manga

    @GET("manga")
    suspend fun getMangaList(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int?,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<MangaList>>>

    @GET
    suspend fun getMangaList(@Url url: String): HttpResponse<Response<List<MangaList>>>

    @GET("users/@me/mangalist")
    suspend fun getUserMangaList(
        @Query("status") status: String?,
        @Query("sort") sort: String?,
        @Query("limit") limit: Int?,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<UserMangaList>>>

    @GET
    suspend fun getUserMangaList(@Url url: String): HttpResponse<Response<List<UserMangaList>>>

    @GET("manga/ranking")
    suspend fun getMangaRanking(
        @Query("ranking_type") rankingType: String,
        @Query("limit") limit: Int,
        @Query("nsfw") nsfw: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<Response<List<MangaRanking>>>

    @GET
    suspend fun getMangaRanking(@Url url: String): HttpResponse<Response<List<MangaRanking>>>

    @FormUrlEncoded
    @PATCH("manga/{id}/my_list_status")
    suspend fun updateUserMangaList(
        @Path("id") mangaId: Int,
        @Field("status") status: String?,
        @Field("score") score: Int?,
        @Field("num_chapters_read") chaptersRead: Int?,
        @Field("num_volumes_read") volumesRead: Int?,
        @Field("start_date") startDate: String?,
        @Field("finish_date") endDate: String?,
        @Field("is_rereading") isRereading: Boolean?,
        @Field("num_times_reread") numRereads: Int?,
        @Field("reread_value") rereadValue: Int?,
        @Field("priority") priority: Int?,
        @Field("tags") tags: String?,
        @Field("comments") comments: String?,
    ): HttpResponse<MyMangaListStatus>

    @DELETE("manga/{id}/my_list_status")
    suspend fun deleteMangaEntry(@Path("id") mangaId: Int): HttpResponse<Unit>

    @GET("manga/{id}")
    suspend fun getMangaDetails(
        @Path("id") mangaId: Int,
        @Query("fields") fields: String?,
    ): HttpResponse<MangaDetails>

    // User

    @GET("users/@me")
    suspend fun getUser(@Query("fields") fields: String?): HttpResponse<User>
}

/**
 * https://myanimelist.net/v1/oauth2/ (login and token refresh). Uses the plain client: no
 * bearer token, no authenticator (a refresh must never trigger another refresh), no logging
 * (the bodies carry tokens).
 */
interface MalAuthService {

    @FormUrlEncoded
    @POST("token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("grant_type") grantType: String,
        @Field("redirect_uri") redirectUri: String,
    ): HttpResponse<AccessToken>

    @FormUrlEncoded
    @POST("token")
    suspend fun refreshAccessToken(
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String,
    ): HttpResponse<AccessToken>
}
