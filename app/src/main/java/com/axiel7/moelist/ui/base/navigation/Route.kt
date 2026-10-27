package com.axiel7.moelist.ui.base.navigation

import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType
import kotlinx.serialization.Serializable

sealed interface Route {
    sealed interface Tab : Route {
        @Serializable
        data object Home : Tab

        @Serializable
        data class Anime(val mediaType: MediaType) : Tab

        @Serializable
        data class Manga(val mediaType: MediaType) : Tab

        @Serializable
        data object More : Tab
    }

    @Serializable
    data class MediaRanking(val mediaType: MediaType) : Route

    @Serializable
    data class MediaDetails(
        val mediaType: MediaType,
        val mediaId: Int,
        // Poster the user tapped, so details can show it instantly and the shared poster
        // transition has something to land on before the details request finishes
        val posterUrl: String? = null,
        // Shared-transition key of the tapped poster (see posterSharedKey), so the details hero
        // flies from exactly that poster. Null = no shared poster transition (e.g. deep links)
        val posterKey: String? = null,
    ) : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object SeasonChart : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class UserList(
        val mediaType: MediaType,
        val status: ListStatus? = null,
    ) : Route

    @Serializable
    data class FullPoster(val pictures: List<String>) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Appearance : Route

    @Serializable
    data object ListStyleSettings : Route

    @Serializable
    data object NavigationSettings : Route

    @Serializable
    data object ContentSettings : Route

    @Serializable
    data object Notifications : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Credits : Route
}
