package com.axiel7.moelist.data.model.recommendation

import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.BaseRecommendation
import com.axiel7.moelist.data.model.media.ListStatus
import kotlin.math.ln

/**
 * The "For You" maths, kept free of Android and network code so it's easy to follow and test.
 *
 * 1. [pickSeeds]: the user's best entries (scored 7+, best first, then most recently updated;
 *    topped up with unscored Completed / Watching entries for people who don't score).
 * 2. [rank]: every title recommended by a seed gets points from that seed. A seed the user
 *    scored higher, and a recommendation more MAL users agreed with, give more points. A title
 *    recommended by several seeds gets a small extra boost. MAL's own suggestions add a fixed
 *    amount. Titles already on the list (or hidden) are left out.
 * 3. The seed that gave a title the most points is its reason ("Because you liked …"), and
 *    each seed with enough picks becomes a group.
 */
object RecommendationEngine {

    const val MAX_SEEDS = 12
    const val MAX_ITEMS = 60
    const val MAX_GROUPS = 6
    const val MIN_GROUP_SIZE = 3
    private const val MAX_GROUP_SIZE = 15

    /** Points a MAL suggestion adds (about what an unscored seed with a few votes gives). */
    private const val SUGGESTION_POINTS = 0.6

    /** Extra share per additional seed recommending the same title. */
    private const val MULTI_SEED_BONUS = 0.15

    /** Lowest score that makes an entry a seed. */
    private const val MIN_SEED_SCORE = 7

    private val SEED_STATUSES = setOf(
        ListStatus.COMPLETED,
        ListStatus.WATCHING,
        ListStatus.READING,
        ListStatus.ON_HOLD,
    )

    /** An entry of the user's list. */
    data class ListEntry(
        val node: BaseMediaNode,
        val status: ListStatus,
        val score: Int,
        val updatedAt: String?,
    )

    fun pickSeeds(list: List<ListEntry>): List<ListEntry> {
        val eligible = list.filter { it.status in SEED_STATUSES }
        val scored = eligible
            .filter { it.score >= MIN_SEED_SCORE }
            .sortedWith(
                compareByDescending<ListEntry> { it.score }
                    .thenByDescending { it.updatedAt.orEmpty() }
            )
        if (scored.size >= MAX_SEEDS) return scored.take(MAX_SEEDS)
        // People who don't score: their most recent finished / ongoing entries
        val unscored = eligible
            .filter { it.score == 0 }
            .sortedWith(
                compareBy<ListEntry> { if (it.status == ListStatus.COMPLETED) 0 else 1 }
                    .thenByDescending { it.updatedAt.orEmpty() }
            )
        return (scored + unscored).take(MAX_SEEDS)
    }

    /** How much a seed counts: 10 → 1.0, 9 → 0.83, 8 → 0.67, 7 → 0.5, unscored → 0.5. */
    private fun seedWeight(score: Int): Double =
        if (score <= 0) 0.5 else (score - 4).coerceAtLeast(1) / 6.0

    /** Small nudge by community score, when MAL sent it (7.0 = neutral). */
    private fun qualityFactor(mean: Float?): Double =
        if (mean == null || mean <= 0f) 1.0
        else (1.0 + 0.04 * (mean - 7f)).coerceIn(0.85, 1.1)

    /**
     * @param seeds from [pickSeeds]
     * @param recommendationsBySeed MAL recommendations of each seed, by seed id
     * @param suggestions MAL's own suggestions for the user (anime only), may be empty
     * @param excludedIds titles on the user's list (any status) and hidden ones
     */
    fun rank(
        seeds: List<ListEntry>,
        recommendationsBySeed: Map<Int, List<BaseRecommendation>>,
        suggestions: List<BaseMediaNode>,
        excludedIds: Set<Int>,
    ): Pair<List<RecommendedMedia>, List<RecommendationGroup>> {
        class Candidate(val node: BaseMediaNode) {
            var numRecommendations = 0
            val contributions = mutableMapOf<Int, Double>() // seed id -> points
            var suggestionPoints = 0.0
        }

        val candidates = linkedMapOf<Int, Candidate>()
        val seedIds = seeds.map { it.node.id }.toSet()

        for (seed in seeds) {
            val weight = seedWeight(seed.score)
            recommendationsBySeed[seed.node.id].orEmpty().forEach { rec ->
                val id = rec.node.id
                if (id in excludedIds || id in seedIds) return@forEach
                val candidate = candidates.getOrPut(id) { Candidate(rec.node) }
                candidate.numRecommendations += rec.numRecommendations
                val points = weight * (1.0 + ln(1.0 + rec.numRecommendations.coerceAtLeast(0)))
                candidate.contributions[seed.node.id] =
                    (candidate.contributions[seed.node.id] ?: 0.0) + points
            }
        }
        suggestions.forEach { node ->
            if (node.id in excludedIds || node.id in seedIds) return@forEach
            candidates.getOrPut(node.id) { Candidate(node) }.suggestionPoints += SUGGESTION_POINTS
        }

        val seedById = seeds.associateBy { it.node.id }
        val scored = candidates.values.map { candidate ->
            val fromSeeds = candidate.contributions.values.sum()
            val multiSeed = 1.0 + MULTI_SEED_BONUS * (candidate.contributions.size - 1).coerceAtLeast(0)
            val total = (fromSeeds * multiSeed + candidate.suggestionPoints) *
                qualityFactor(candidate.node.mean)
            val reason = candidate.contributions.maxByOrNull { it.value }?.key?.let { seedById[it] }
            candidate to RecommendedMedia.from(
                node = candidate.node,
                numRecommendations = candidate.numRecommendations,
                score = total,
                reason = reason?.node,
            )
        }
            .sortedByDescending { it.second.score }
            .take(MAX_ITEMS)

        val items = scored.map { it.second }
        val kept = scored.associate { it.second.id to it.first }

        val groups = seeds.mapNotNull { seed ->
            val ids = kept
                .filter { (_, candidate) -> seed.node.id in candidate.contributions }
                .entries
                .sortedByDescending { it.value.contributions[seed.node.id] }
                .map { it.key }
                .take(MAX_GROUP_SIZE)
            if (ids.size < MIN_GROUP_SIZE) null
            else RecommendationGroup(
                seedId = seed.node.id,
                seedTitle = seed.node.title,
                seedTitleEn = seed.node.alternativeTitles?.en,
                seedTitleJa = seed.node.alternativeTitles?.ja,
                seedPicture = seed.node.mainPicture?.large ?: seed.node.mainPicture?.medium,
                itemIds = ids,
            )
        }.take(MAX_GROUPS)

        return items to groups
    }

    /** Changes when an entry is added, removed, rescored or moves to another status. */
    fun fingerprint(list: List<ListEntry>, showNsfw: Boolean): Int =
        (list.map { "${it.node.id}:${it.status.name}:${it.score}" }.sorted() + "nsfw=$showNsfw")
            .hashCode()
}
