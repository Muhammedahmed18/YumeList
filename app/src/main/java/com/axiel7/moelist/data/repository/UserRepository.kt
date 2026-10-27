package com.axiel7.moelist.data.repository

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.User
import com.axiel7.moelist.data.model.UserStats
import com.axiel7.moelist.data.model.manga.UserMangaList
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.JikanApi
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

class UserRepository(
    private val api: Api,
    private val jikanApi: JikanApi
) {

    companion object {
        private const val USER_FIELDS = "id,name,gender,location,joined_at,anime_statistics,manga_statistics"

        // Only what the stats need, so even big lists stay small
        private const val MANGA_STATS_FIELDS =
            "list_status{status,score,num_chapters_read,num_volumes_read,num_times_reread}"
        private const val MANGA_STATS_PAGE_SIZE = 1000

        // Safety stop: 20 pages = 20,000 entries
        private const val MANGA_STATS_MAX_PAGES = 20
    }

    suspend fun getMyUser(): User? {
        return try {
            api.getUser(USER_FIELDS)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserStats(
        username: String
    ): Response<UserStats> {
        return try {
            jikanApi.getUserStats(username)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Response(message = e.message)
        }
    }

    /**
     * Manga statistics of the logged-in user, counted from their own MAL manga list (every
     * status, NSFW included, like MAL's profile page). MAL's official API has no manga
     * statistics, so this replaces depending on Jikan for them.
     *
     * `days` is left `null`: MAL doesn't return it per entry. The caller fills it from Jikan.
     * On failure the [Response] carries the real error (HTTP code / network message).
     */
    suspend fun getMyMangaStats(): Response<UserStats.MangaStats> {
        return try {
            val entries = mutableListOf<UserMangaList>()
            var result = api.getUserMangaList(
                status = null,
                sort = null,
                limit = MANGA_STATS_PAGE_SIZE,
                nsfw = 1,
                fields = MANGA_STATS_FIELDS,
            )
            var pages = 1
            while (true) {
                val data = result.data
                if (result.error != null || data == null) {
                    return Response(error = result.error, message = result.message)
                }
                entries += data
                val next = result.paging?.next
                if (next == null || pages >= MANGA_STATS_MAX_PAGES) break
                result = api.getUserMangaList(next)
                pages++
            }
            Response(data = entries.toMangaStats())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Response(message = e.message ?: e.javaClass.simpleName)
        }
    }

    private fun List<UserMangaList>.toMangaStats(): UserStats.MangaStats {
        val statuses = mapNotNull { it.listStatus }
        val scores = statuses.map { it.score }.filter { it > 0 }
        return UserStats.MangaStats(
            days = null,
            // MAL shows the mean of scored entries, to two decimals
            meanScore = if (scores.isEmpty()) 0f else (scores.average() * 100).roundToInt() / 100f,
            current = statuses.count { it.status == ListStatus.READING },
            completed = statuses.count { it.status == ListStatus.COMPLETED },
            onHold = statuses.count { it.status == ListStatus.ON_HOLD },
            dropped = statuses.count { it.status == ListStatus.DROPPED },
            planned = statuses.count { it.status == ListStatus.PLAN_TO_READ },
            totalEntries = statuses.size,
            repeat = statuses.sumOf { it.repeatCount ?: 0 },
            chaptersRead = statuses.sumOf { it.progress ?: 0 },
            volumesRead = statuses.sumOf { it.numVolumesRead },
        )
    }
}
