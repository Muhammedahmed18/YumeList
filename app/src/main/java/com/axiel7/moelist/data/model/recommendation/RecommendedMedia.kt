package com.axiel7.moelist.data.model.recommendation

import androidx.compose.runtime.Immutable
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.media.BaseMediaNode
import com.axiel7.moelist.data.model.media.MainPicture
import com.axiel7.moelist.data.model.media.MediaFormat
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.media.TitleLanguage
import kotlinx.serialization.Serializable

/**
 * A title picked for the user, flattened so it can be cached as JSON and shown by any screen
 * without caring whether it's anime or manga.
 *
 * @param score how strongly it's recommended (only used for ordering)
 * @param reasonId / reasonTitle the entry of the user's list that contributed most to the pick
 * ("Because you liked …"); null for picks that aren't based on the user's list
 */
@Immutable
@Serializable
data class RecommendedMedia(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val titleEn: String? = null,
    val titleJa: String? = null,
    val picture: String? = null,
    val mediaFormat: MediaFormat? = null,
    val mean: Float? = null,
    val numRecommendations: Int = 0,
    val score: Double = 0.0,
    val reasonId: Int? = null,
    val reasonTitle: String? = null,
    val reasonTitleEn: String? = null,
    val reasonTitleJa: String? = null,
) {
    fun userPreferredTitle() = preferredTitle(title, titleEn, titleJa)

    fun reasonPreferredTitle(): String? =
        reasonTitle?.let { preferredTitle(it, reasonTitleEn, reasonTitleJa) }

    companion object {
        fun from(
            node: BaseMediaNode,
            numRecommendations: Int = 0,
            score: Double = 0.0,
            reason: BaseMediaNode? = null,
        ) = RecommendedMedia(
            id = node.id,
            mediaType = node.mediaType,
            title = node.title,
            titleEn = node.alternativeTitles?.en,
            titleJa = node.alternativeTitles?.ja,
            picture = node.mainPicture?.large ?: node.mainPicture?.medium,
            mediaFormat = node.mediaFormat,
            mean = node.mean,
            numRecommendations = numRecommendations,
            score = score,
            reasonId = reason?.id,
            reasonTitle = reason?.title,
            reasonTitleEn = reason?.alternativeTitles?.en,
            reasonTitleJa = reason?.alternativeTitles?.ja,
        )

        private fun preferredTitle(romaji: String, en: String?, ja: String?) =
            when (App.titleLanguage) {
                TitleLanguage.ROMAJI -> romaji
                TitleLanguage.ENGLISH -> en?.takeIf { it.isNotBlank() } ?: romaji
                TitleLanguage.JAPANESE -> ja?.takeIf { it.isNotBlank() } ?: romaji
            }
    }
}

/** One "Because you liked [seedTitle]" row: the picks that this entry of the list led to. */
@Immutable
@Serializable
data class RecommendationGroup(
    val seedId: Int,
    val seedTitle: String,
    val seedTitleEn: String? = null,
    val seedTitleJa: String? = null,
    val seedPicture: String? = null,
    val itemIds: List<Int>,
) {
    fun seedPreferredTitle() = RecommendedMedia(
        id = seedId,
        mediaType = MediaType.ANIME,
        title = seedTitle,
        titleEn = seedTitleEn,
        titleJa = seedTitleJa,
    ).userPreferredTitle()
}

/**
 * Result of a "For You" computation, as cached.
 *
 * @param kind [Kind.PERSONAL] when built from the user's list, otherwise a labelled fallback
 * @param fingerprint hash of the list it was built from, to tell when the list changed
 */
@Immutable
@Serializable
data class ForYouResult(
    val mediaType: MediaType,
    val kind: Kind,
    val items: List<RecommendedMedia>,
    val groups: List<RecommendationGroup> = emptyList(),
    val computedAt: Long = 0,
    val fingerprint: Int = 0,
) {
    @Serializable
    enum class Kind {
        /** Built from the user's own list ("For You", "Because you liked …"). */
        PERSONAL,

        /** Logged in but the list has nothing to build from yet: popular titles they don't have. */
        STARTER,

        /** Logged out: popular titles ("Popular right now"). */
        POPULAR,
    }

    val isPersonal get() = kind == Kind.PERSONAL

    fun itemsOf(group: RecommendationGroup): List<RecommendedMedia> {
        val byId = items.associateBy { it.id }
        return group.itemIds.mapNotNull { byId[it] }
    }

    /** Copy without the given titles (hidden or no longer wanted). */
    fun without(ids: Set<Int>): ForYouResult {
        if (ids.isEmpty()) return this
        return copy(
            items = items.filterNot { it.id in ids },
            groups = groups
                .map { group -> group.copy(itemIds = group.itemIds.filterNot { it in ids }) }
                .filter { it.itemIds.size >= RecommendationEngine.MIN_GROUP_SIZE },
        )
    }
}
