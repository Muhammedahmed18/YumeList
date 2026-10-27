package com.axiel7.moelist.ui.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaType

@Immutable
class NavActionManager(
    private val navController: NavHostController
) {
    fun goBack() {
        navController.popBackStack()
    }

    fun toMediaRanking(mediaType: MediaType) {
        navController.navigate(Route.MediaRanking(mediaType))
    }

    /**
     * @param posterUrl the poster image the user tapped (optional). Passing it enables the
     * shared poster transition and shows the poster on the details screen immediately.
     * @param posterKey shared-transition key of the tapped poster (see posterSharedKey).
     * Without it the details screen opens with no shared poster transition.
     */
    fun toMediaDetails(
        mediaType: MediaType,
        id: Int,
        posterUrl: String? = null,
        posterKey: String? = null,
    ) {
        navController.navigate(
            Route.MediaDetails(
                mediaType = mediaType,
                mediaId = id,
                posterUrl = posterUrl,
                posterKey = posterKey,
            )
        )
    }

    fun toCalendar() {
        navController.navigate(Route.Calendar)
    }

    fun toSeasonChart() {
        navController.navigate(Route.SeasonChart)
    }

    fun toUserList(mediaType: MediaType, status: ListStatus? = null) {
        navController.navigate(Route.UserList(mediaType, status))
    }

    fun toFullPoster(pictures: List<String>) {
        navController.navigate(Route.FullPoster(pictures))
    }

    fun toSettings() {
        navController.navigate(Route.Settings)
    }

    fun toAppearance() {
        navController.navigate(Route.Appearance)
    }

    fun toListStyleSettings() {
        navController.navigate(Route.ListStyleSettings)
    }

    fun toNavigationSettings() {
        navController.navigate(Route.NavigationSettings)
    }

    fun toContentSettings() {
        navController.navigate(Route.ContentSettings)
    }

    fun toNotifications() {
        navController.navigate(Route.Notifications)
    }

    fun toAbout() {
        navController.navigate(Route.About)
    }

    fun toCredits() {
        navController.navigate(Route.Credits)
    }

    fun toProfile() {
        navController.navigate(Route.Profile)
    }

    fun toLogin() {
        navController.navigate(Route.Profile)
    }

    companion object {
        @Composable
        fun rememberNavActionManager(
            navController: NavHostController = rememberNavController()
        ) = remember {
            NavActionManager(navController)
        }
    }
}
