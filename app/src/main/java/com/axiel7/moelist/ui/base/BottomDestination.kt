package com.axiel7.moelist.ui.base

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.ui.base.navigation.Route
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.automirrored.outlined.MenuBook
import com.axiel7.moelist.ui.icons.automirrored.rounded.MenuBook
import com.axiel7.moelist.ui.icons.filled.Tv
import com.axiel7.moelist.ui.icons.outlined.Home
import com.axiel7.moelist.ui.icons.outlined.Person
import com.axiel7.moelist.ui.icons.outlined.Tv
import com.axiel7.moelist.ui.icons.rounded.Home
import com.axiel7.moelist.ui.icons.rounded.Person

sealed class BottomDestination(
    val value: String,
    val route: Any,
    @StringRes val title: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector,
) {
    data object Home : BottomDestination(
        value = "home",
        route = Route.Tab.Home,
        title = R.string.title_home,
        icon = Icons.Outlined.Home,
        iconSelected = Icons.Rounded.Home
    )

    data object AnimeList : BottomDestination(
        value = "anime",
        route = Route.Tab.Anime(mediaType = MediaType.ANIME),
        title = R.string.title_anime_list,
        // A TV for anime and an open book for manga; outlined until selected, then filled
        icon = Icons.Outlined.Tv,
        iconSelected = Icons.Filled.Tv
    )

    data object MangaList : BottomDestination(
        value = "manga",
        route = Route.Tab.Manga(MediaType.MANGA),
        title = R.string.title_manga_list,
        icon = Icons.AutoMirrored.Outlined.MenuBook,
        iconSelected = Icons.AutoMirrored.Rounded.MenuBook
    )

    data object Profile : BottomDestination(
        value = "profile",
        route = Route.Profile,
        title = R.string.title_profile,
        icon = Icons.Outlined.Person,
        iconSelected = Icons.Rounded.Person
    )

    companion object {
        // The old "More" tab is now the Account sheet (avatar on Home)
        val values = listOf(Home, AnimeList, MangaList)

        fun String.toBottomDestinationIndex() = when (this) {
            Home.value -> 0
            AnimeList.value -> 1
            MangaList.value -> 2
            Profile.value -> 4
            else -> null
        }

        fun NavBackStackEntry.isBottomDestination() =
            destination.hierarchy.any { dest ->
                values.any { value -> dest.hasRoute(value.route::class) }
            }

        @Composable
        fun BottomDestination.Icon(selected: Boolean) {
            androidx.compose.material3.Icon(
                imageVector = if (selected) iconSelected else icon,
                contentDescription = stringResource(title)
            )
        }
    }
}
