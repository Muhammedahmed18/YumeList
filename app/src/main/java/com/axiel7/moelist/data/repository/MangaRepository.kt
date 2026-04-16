package com.axiel7.moelist.data.repository

import androidx.annotation.IntRange
import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.manga.MangaDetails
import com.axiel7.moelist.data.model.manga.MangaList
import com.axiel7.moelist.data.model.manga.MangaRanking
import com.axiel7.moelist.data.model.manga.MyMangaListStatus
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.network.Api
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MangaRepository(
    private val api: Api,
    private val defaultPreferencesRepository: DefaultPreferencesRepository
) : BaseRepository(api, defaultPreferencesRepository) {

    private val _userMangaList = MutableStateFlow<List<UserMangaList>>(emptyList())
    val userMangaList = _userMangaList.asStateFlow()

    companion object {
        private const val LIST_STATUS_FIELDS =
            "start_date,finish_date,num_times_reread,is_rereading,reread_value,priority,tags,comments"
        private const val MANGA_DETAILS_FIELDS =
            "id,title,main_picture,pictures,alternative_titles,start_date,end_date," +
                    "synopsis,mean,rank,popularity,num_list_users,num_scoring_users,media_type,status,genres," +
                    "my_list_status{$LIST_STATUS_FIELDS},num_chapters,num_volumes,source,authors{first_name,last_name}," +
                    "serialization,related_anime{media_type,alternative_titles{en,ja}}," +
                    "related_manga{media_type,alternative_titles{en,ja}}," +
                    "recommendations{alternative_titles{en,ja}},background"
        private const val USER_MANGA_LIST_FIELDS =
            "alternative_titles{en,ja},list_status{$LIST_STATUS_FIELDS},num_chapters,num_volumes,media_type,status"
        private const val SEARCH_FIELDS =
            "id,title,alternative_titles{en,ja},main_picture,mean,media_type,num_chapters,start_date," +
                    "my_list_status{status}"
        private const val RANKING_FIELDS =
            "alternative_titles{en,ja},mean,media_type,num_chapters,num_list_users,my_list_status{status}"
    }

    suspend fun getMangaDetails(
        mangaId: Int
    ): MangaDetails? {
        return try {
            api.getMangaDetails(mangaId, MANGA_DETAILS_FIELDS)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUserMangaList(
        status: ListStatus,
        sort: MediaSort,
        page: String? = null
    ): Response<List<UserMangaList>> {
        return try {
            val result = if (page == null) api.getUserMangaList(
                status = status,
                sort = sort,
                limit = null,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = USER_MANGA_LIST_FIELDS,
            )
            else api.getUserMangaList(page)
            val retry = result.error?.let { handleResponseError(it) }
            if (retry == true) return getUserMangaList(status, sort, page)

            if (result.data != null) {
                _userMangaList.update { currentList ->
                    val newList = currentList.toMutableList()
                    if (page == null) {
                        newList.removeAll { it.listStatus?.status == status }
                    }
                    result.data.forEach { newItem ->
                        // Remove item if it exists anywhere in the list (even with different status)
                        // to ensure it takes the new position provided by the API sort
                        newList.removeAll { it.node.id == newItem.node.id }
                        newList.add(newItem)
                    }
                    newList
                }
            }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun updateMangaEntry(
        mangaId: Int,
        status: ListStatus? = null,
        @IntRange(0, 10) score: Int? = null,
        chaptersRead: Int? = null,
        volumesRead: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        isRereading: Boolean? = null,
        numRereads: Int? = null,
        @IntRange(0, 5) rereadValue: Int? = null,
        @IntRange(0, 2) priority: Int? = null,
        tags: String? = null,
        comments: String? = null,
    ): MyMangaListStatus? {
        // Optimistic update
        val previousList = _userMangaList.value
        _userMangaList.update { currentList ->
            currentList.map {
                if (it.node.id == mangaId) {
                    it.copy(
                        listStatus = it.listStatus?.copy(
                            status = status ?: it.listStatus.status,
                            score = score ?: it.listStatus.score,
                            progress = chaptersRead ?: it.listStatus.progress,
                            numVolumesRead = volumesRead ?: it.listStatus.numVolumesRead,
                            startDate = startDate ?: it.listStatus.startDate,
                            finishDate = endDate ?: it.listStatus.finishDate,
                            isRepeating = isRereading ?: it.listStatus.isRepeating,
                            repeatCount = numRereads ?: it.listStatus.repeatCount,
                            repeatValue = rereadValue ?: it.listStatus.repeatValue,
                            priority = priority ?: it.listStatus.priority,
                            tags = tags?.split(",") ?: it.listStatus.tags,
                            comments = comments ?: it.listStatus.comments
                        )
                    )
                } else it
            }
        }

        return try {
            val result = api.updateUserMangaList(
                mangaId,
                status,
                score,
                chaptersRead,
                volumesRead,
                startDate,
                endDate,
                isRereading,
                numRereads,
                rereadValue,
                priority,
                tags,
                comments
            )
            val retry = result.error?.let { handleResponseError(it) }
            if (retry == true) {
                return updateMangaEntry(
                    mangaId,
                    status,
                    score,
                    chaptersRead,
                    volumesRead,
                    startDate,
                    endDate,
                    isRereading,
                    numRereads,
                    rereadValue,
                    priority,
                    tags,
                    comments
                )
            }
            _userMangaList.update { currentList ->
                currentList.map {
                    if (it.node.id == mangaId) it.copy(listStatus = result) else it
                }
            }
            result
        } catch (_: Exception) {
            _userMangaList.value = previousList
            null
        }
    }

    suspend fun deleteMangaEntry(
        mangaId: Int
    ): Boolean {
        return try {
            val result = api.deleteMangaEntry(mangaId)
            if (result.status == HttpStatusCode.OK) {
                _userMangaList.update { currentList ->
                    currentList.filter { it.node.id != mangaId }
                }
                return true
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun searchManga(
        query: String,
        limit: Int,
        offset: Int? = null,
        page: String? = null
    ): Response<List<MangaList>> {
        return try {
            val result = if (page == null) api.getMangaList(
                query = query,
                limit = limit,
                offset = offset,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = SEARCH_FIELDS,
            )
            else api.getMangaList(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getMangaRanking(
        rankingType: RankingType,
        limit: Int,
        page: String? = null
    ): Response<List<MangaRanking>> {
        return try {
            val result =
                if (page == null) api.getMangaRanking(
                    rankingType = rankingType.serialName,
                    limit = limit,
                    nsfw = defaultPreferencesRepository.nsfwInt(),
                    fields = RANKING_FIELDS,
                )
                else api.getMangaRanking(page)
            result.error?.let { handleResponseError(it) }
            return result
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    suspend fun getMangaIdsOfUserList(
        status: ListStatus,
        prefetchedList: List<UserMangaList> = emptyList(),
        page: String? = null
    ): Response<List<Int>> {
        return try {
            val result = if (page == null) api.getUserMangaList(
                status = status,
                sort = MediaSort.UPDATED,
                limit = 1000,
                nsfw = defaultPreferencesRepository.nsfwInt(),
                fields = "id",
            ) else api.getUserMangaList(page)
            result.error?.let {
                handleResponseError(it)
                return Response(error = result.error, message = result.message)
            }
            if (result.paging?.next != null) {
                getMangaIdsOfUserList(
                    status = status,
                    prefetchedList = prefetchedList.plus(result.data.orEmpty()),
                    page = result.paging.next
                )
            } else return Response(
                data = prefetchedList.plus(result.data.orEmpty()).map { it.node.id }
            )
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }
}
