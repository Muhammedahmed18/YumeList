package com.axiel7.moelist.data.repository

import com.axiel7.moelist.data.model.Response
import com.axiel7.moelist.data.model.anime.AnimeNode
import com.axiel7.moelist.data.model.manga.MangaNode
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseRecommendation
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.RankingType
import com.axiel7.moelist.data.model.recommendation.ForYouResult
import com.axiel7.moelist.data.model.recommendation.RecommendationEngine
import com.axiel7.moelist.data.model.recommendation.RecommendationEngine.ListEntry
import com.axiel7.moelist.data.model.recommendation.RecommendedMedia
import com.axiel7.moelist.data.network.Api
import com.axiel7.moelist.data.network.NetworkJson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * "For You" recommendations, for anime and manga.
 *
 * Building them takes the user's whole list plus one small details request per seed (see
 * [RecommendationEngine]), so the result is cached:
 * - saved on the device, so Home shows it straight away ([peekForYou]),
 * - the list is re-checked at most once an hour; when it changed in a way that matters
 *   (an entry added, removed, rescored or moved), or the result is a day old, it's rebuilt,
 * - pull-to-refresh forces a rebuild.
 *
 * Titles marked "Not interested" are kept apart and never shown again.
 */
class RecommendationRepository(
    private val api: Api,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
) {
    private val locks = mapOf(MediaType.ANIME to Mutex(), MediaType.MANGA to Mutex())

    /** Last result per type, so screens don't decode the saved JSON again. */
    private val memory = ConcurrentHashMap<MediaType, ForYouResult>()

    /** When the list was last compared with the cached result, per type. */
    private val lastChecked = ConcurrentHashMap<MediaType, Long>()

    /** Logged-out results ("Popular right now"), kept in memory only. */
    private val popularMemory = ConcurrentHashMap<MediaType, ForYouResult>()

    /**
     * The saved result, without hidden titles, for showing something before [getForYou]
     * finishes. Null when there's none (or it isn't a personal one and [isLoggedIn]).
     */
    suspend fun peekForYou(mediaType: MediaType, isLoggedIn: Boolean): ForYouResult? {
        val result = if (isLoggedIn) cached(mediaType) else popularMemory[mediaType]
        return result?.without(hiddenIds(mediaType))
    }

    /**
     * Recommendations for [mediaType]: personal ones when logged in, popular titles otherwise.
     * Uses the cache unless it's stale or [forceRefresh]. When a rebuild fails but an older
     * result exists, the older one is returned rather than an error.
     */
    suspend fun getForYou(
        mediaType: MediaType,
        isLoggedIn: Boolean,
        forceRefresh: Boolean = false,
    ): Response<ForYouResult> = locks.getValue(mediaType).withLock {
        try {
            val hidden = hiddenIds(mediaType)
            if (!isLoggedIn) return@withLock popular(mediaType, hidden, forceRefresh)

            val now = System.currentTimeMillis()
            val saved = cached(mediaType)
            if (!forceRefresh && saved != null) {
                val age = now - saved.computedAt
                val checkedAgo = now - (lastChecked[mediaType] ?: saved.computedAt)
                if (age < MAX_AGE_MS && checkedAgo < CHECK_INTERVAL_MS) {
                    return@withLock Response(data = saved.without(hidden))
                }
            }

            val listResponse = loadUserList(mediaType)
            val list = listResponse.data
            if (list == null) {
                return@withLock if (saved != null) Response(data = saved.without(hidden))
                else Response(error = listResponse.error, message = listResponse.message)
            }
            lastChecked[mediaType] = now

            val showNsfw = defaultPreferencesRepository.nsfw.first()
            val fingerprint = RecommendationEngine.fingerprint(list, showNsfw)
            if (!forceRefresh && saved != null && saved.fingerprint == fingerprint &&
                now - saved.computedAt < MAX_AGE_MS
            ) {
                return@withLock Response(data = saved.without(hidden))
            }

            val built = build(mediaType, list, hidden, showNsfw)
            if (built == null) {
                return@withLock if (saved != null) Response(data = saved.without(hidden))
                else Response(message = "Couldn't load recommendations")
            }
            val result = built.copy(computedAt = now, fingerprint = fingerprint)
            save(mediaType, result)
            Response(data = result.without(hidden))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val fallback = cached(mediaType)
            if (fallback != null) Response(data = fallback.without(hiddenIds(mediaType)))
            else Response(message = e.message)
        }
    }

    /** "Not interested": never recommend this title again. */
    suspend fun hide(mediaType: MediaType, id: Int) {
        defaultPreferencesRepository.addHiddenRecommendation(mediaType, id)
    }

    /** Undo of [hide]. */
    suspend fun unhide(mediaType: MediaType, id: Int) {
        defaultPreferencesRepository.removeHiddenRecommendation(mediaType, id)
    }

    /**
     * Adds a recommended title to Plan to Watch / Plan to Read.
     * @return null when it worked, otherwise what went wrong
     */
    suspend fun addToPlan(item: RecommendedMedia): Throwable? {
        var error: Throwable? = null
        val result = if (item.mediaType == MediaType.ANIME) {
            animeRepository.updateAnimeEntry(
                animeId = item.id,
                status = ListStatus.PLAN_TO_WATCH,
                onError = { error = it },
            )
        } else {
            mangaRepository.updateMangaEntry(
                mangaId = item.id,
                status = ListStatus.PLAN_TO_READ,
                onError = { error = it },
            )
        }
        return if (result != null) null else error ?: IllegalStateException()
    }

    /**
     * Status of each title on the user's list that the app already knows about (what was loaded
     * in My List, plus everything changed in the app), so recommendation cards can show it.
     */
    fun listStatuses(mediaType: MediaType): Flow<Map<Int, ListStatus>> {
        val statuses: Flow<Map<Int, ListStatus>> = if (mediaType == MediaType.ANIME) {
            animeRepository.userAnimeList.map { list ->
                list.mapNotNull { item -> item.listStatus?.status?.let { item.node.id to it } }.toMap()
            }
        } else {
            mangaRepository.userMangaList.map { list ->
                list.mapNotNull { item -> item.listStatus?.status?.let { item.node.id to it } }.toMap()
            }
        }
        return statuses.distinctUntilChanged()
    }

    // ── Building ─────────────────────────────────────────────────────────────

    /** Null when nothing could be loaded at all. */
    private suspend fun build(
        mediaType: MediaType,
        list: List<ListEntry>,
        hidden: Set<Int>,
        showNsfw: Boolean,
    ): ForYouResult? {
        val seeds = RecommendationEngine.pickSeeds(list)
        val onList = list.map { it.node.id }.toSet()

        val failedSeeds = AtomicInteger(0)
        val recommendationsBySeed: Map<Int, List<BaseRecommendation>> = coroutineScope {
            val permits = Semaphore(MAX_PARALLEL_REQUESTS)
            seeds.map { seed ->
                async {
                    permits.withPermit {
                        val recommendations = seedRecommendations(mediaType, seed.node.id)
                        if (recommendations == null) failedSeeds.incrementAndGet()
                        seed.node.id to recommendations.orEmpty()
                    }
                }
            }.awaitAll().toMap()
        }

        val suggestions = if (mediaType == MediaType.ANIME) animeSuggestions() else emptyList()

        val adult = if (showNsfw) emptySet() else {
            (recommendationsBySeed.values.flatten().map { it.node } + suggestions)
                .filter { it.isAdult() }
                .map { it.id }
                .toSet()
        }
        val excluded = onList + hidden + adult

        val (items, groups) = RecommendationEngine.rank(
            seeds = seeds,
            recommendationsBySeed = recommendationsBySeed,
            suggestions = suggestions,
            excludedIds = excluded,
        )

        val hasSeedPicks = items.any { it.reasonId != null }
        if (seeds.isNotEmpty() && hasSeedPicks) {
            return ForYouResult(
                mediaType = mediaType,
                kind = ForYouResult.Kind.PERSONAL,
                items = items,
                groups = groups,
            )
        }
        // Every seed failed: report it instead of quietly showing the starter list
        if (seeds.isNotEmpty() && failedSeeds.get() == seeds.size && suggestions.isEmpty()) return null

        // Nothing to build from yet: MAL's suggestions (anime) and popular titles they don't have
        val popular = popularNodes(mediaType)
            .filterNot { it.id in excluded || (!showNsfw && it.isAdult()) }
        val starter = (items + popular.map { RecommendedMedia.from(it) })
            .distinctBy { it.id }
            .take(STARTER_SIZE)
        if (starter.isEmpty() && popular.isEmpty() && suggestions.isEmpty()) return null
        return ForYouResult(
            mediaType = mediaType,
            kind = ForYouResult.Kind.STARTER,
            items = starter,
        )
    }

    private suspend fun popular(
        mediaType: MediaType,
        hidden: Set<Int>,
        forceRefresh: Boolean,
    ): Response<ForYouResult> {
        val now = System.currentTimeMillis()
        val cached = popularMemory[mediaType]
        if (!forceRefresh && cached != null && now - cached.computedAt < POPULAR_MAX_AGE_MS) {
            return Response(data = cached.without(hidden))
        }
        val nodes = popularNodes(mediaType)
        if (nodes.isEmpty()) {
            return if (cached != null) Response(data = cached.without(hidden))
            else Response(message = "Couldn't load recommendations")
        }
        val result = ForYouResult(
            mediaType = mediaType,
            kind = ForYouResult.Kind.POPULAR,
            items = nodes.map { RecommendedMedia.from(it) },
            computedAt = now,
        )
        popularMemory[mediaType] = result
        return Response(data = result.without(hidden))
    }

    // ── Network ──────────────────────────────────────────────────────────────

    /** The user's whole list (every status), or the error. */
    private suspend fun loadUserList(mediaType: MediaType): Response<List<ListEntry>> {
        val entries = mutableListOf<ListEntry>()
        var pages = 0
        if (mediaType == MediaType.ANIME) {
            var response = api.getUserAnimeList(
                status = null,
                sort = null,
                limit = LIST_PAGE_SIZE,
                // Everything on the list, so nothing already on it is recommended
                nsfw = 1,
                fields = LIST_FIELDS,
            )
            while (true) {
                val data = response.data
                    ?: return Response(error = response.error, message = response.message)
                data.forEach { item ->
                    val status = item.listStatus ?: return@forEach
                    entries += ListEntry(item.node, status.status, status.score, status.updatedAt)
                }
                val next = response.paging?.next
                if (next == null || ++pages >= MAX_LIST_PAGES) break
                response = api.getUserAnimeList(next)
            }
        } else {
            var response = api.getUserMangaList(
                status = null,
                sort = null,
                limit = LIST_PAGE_SIZE,
                nsfw = 1,
                fields = LIST_FIELDS,
            )
            while (true) {
                val data = response.data
                    ?: return Response(error = response.error, message = response.message)
                data.forEach { item ->
                    val status = item.listStatus ?: return@forEach
                    entries += ListEntry(item.node, status.status, status.score, status.updatedAt)
                }
                val next = response.paging?.next
                if (next == null || ++pages >= MAX_LIST_PAGES) break
                response = api.getUserMangaList(next)
            }
        }
        return Response(data = entries)
    }

    /** A seed's MAL recommendations; null when the request failed. */
    private suspend fun seedRecommendations(
        mediaType: MediaType,
        id: Int,
    ): List<BaseRecommendation>? = try {
        if (mediaType == MediaType.ANIME) {
            api.getAnimeDetails(id, SEED_FIELDS).recommendations.orEmpty()
        } else {
            api.getMangaDetails(id, SEED_FIELDS).recommendations.orEmpty()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private suspend fun animeSuggestions(): List<BaseMediaNode> = try {
        api.getAnimeSuggestions(
            limit = SUGGESTIONS_LIMIT,
            offset = null,
            nsfw = defaultPreferencesRepository.nsfwInt(),
            fields = NODE_FIELDS,
        ).data?.map { it.node }.orEmpty()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        emptyList()
    }

    private suspend fun popularNodes(mediaType: MediaType): List<BaseMediaNode> = try {
        if (mediaType == MediaType.ANIME) {
            animeRepository.getAnimeRanking(
                rankingType = RankingType.POPULARITY,
                limit = POPULAR_LIMIT,
                fields = NODE_FIELDS,
            ).data?.map { it.node }.orEmpty()
        } else {
            mangaRepository.getMangaRanking(
                rankingType = RankingType.POPULARITY,
                limit = POPULAR_LIMIT,
            ).data?.map { it.node }.orEmpty()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        emptyList()
    }

    // ── Cache ────────────────────────────────────────────────────────────────

    private suspend fun cached(mediaType: MediaType): ForYouResult? {
        memory[mediaType]?.let { return it }
        val json = defaultPreferencesRepository.getForYouCache(mediaType) ?: return null
        val result = runCatching {
            NetworkJson.decodeFromString(ForYouResult.serializer(), json)
        }.getOrNull()
            // A starter list is only a placeholder: try again for a personal one
            ?.takeIf { it.kind != ForYouResult.Kind.POPULAR }
        if (result != null) memory[mediaType] = result
        return result
    }

    private suspend fun save(mediaType: MediaType, result: ForYouResult) {
        memory[mediaType] = result
        runCatching {
            defaultPreferencesRepository.setForYouCache(
                mediaType,
                NetworkJson.encodeToString(ForYouResult.serializer(), result)
            )
        }
    }

    /** Forget the cached results (e.g. after logging out or switching account). */
    fun clearMemory() {
        memory.clear()
        lastChecked.clear()
    }

    private suspend fun hiddenIds(mediaType: MediaType) =
        defaultPreferencesRepository.hiddenRecommendationIds(mediaType).first()

    private fun BaseMediaNode.isAdult() = when (this) {
        is AnimeNode -> nsfw == "black"
        is MangaNode -> nsfw == "black"
        else -> false
    }

    companion object {
        private const val MAX_PARALLEL_REQUESTS = 4
        private const val LIST_PAGE_SIZE = 1000
        private const val MAX_LIST_PAGES = 10
        private const val SUGGESTIONS_LIMIT = 30
        private const val POPULAR_LIMIT = 40
        private const val STARTER_SIZE = 30

        private const val CHECK_INTERVAL_MS = 60 * 60 * 1000L // 1 hour
        private const val MAX_AGE_MS = 24 * 60 * 60 * 1000L // 1 day
        private const val POPULAR_MAX_AGE_MS = 6 * 60 * 60 * 1000L // 6 hours

        private const val NODE_FIELDS = "alternative_titles{en,ja},media_type,mean,nsfw"
        private const val LIST_FIELDS =
            "alternative_titles{en,ja},media_type,mean,list_status{status,score,updated_at}"
        private const val SEED_FIELDS =
            "recommendations{${AnimeRepository.RECOMMENDATION_NODE_FIELDS}}"
    }
}
