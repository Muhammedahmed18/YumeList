package com.axiel7.moelist.data.model.media

import android.os.Bundle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.base.LocalizableAndColorable
import com.axiel7.moelist.ui.icons.Icons
import com.axiel7.moelist.ui.icons.rounded.CheckCircleOutline
import com.axiel7.moelist.ui.icons.rounded.HighlightOff
import com.axiel7.moelist.ui.icons.rounded.PauseCircleOutline
import com.axiel7.moelist.ui.icons.rounded.PlayCircleOutline
import com.axiel7.moelist.ui.icons.rounded.Schedule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ListStatus(
    val icon: ImageVector
) : LocalizableAndColorable {
    @SerialName("watching")
    WATCHING(
        icon = Icons.Rounded.PlayCircleOutline
    ),

    @SerialName("reading")
    READING(
        icon = Icons.Rounded.PlayCircleOutline
    ),

    @SerialName("plan_to_watch")
    PLAN_TO_WATCH(
        icon = Icons.Rounded.Schedule
    ),

    @SerialName("plan_to_read")
    PLAN_TO_READ(
        icon = Icons.Rounded.Schedule
    ),

    @SerialName("completed")
    COMPLETED(
        icon = Icons.Rounded.CheckCircleOutline
    ),

    @SerialName("on_hold")
    ON_HOLD(
        icon = Icons.Rounded.PauseCircleOutline
    ),

    @SerialName("dropped")
    DROPPED(
        icon = Icons.Rounded.HighlightOff
    );

    val value get() = name.lowercase()

    fun isCurrent() = this == WATCHING || this == READING

    fun isPlanning() = this == PLAN_TO_WATCH || this == PLAN_TO_READ

    @Composable
    override fun localized() = stringResource(stringRes)

    val stringRes
        get() = when (this) {
            WATCHING -> R.string.watching
            READING -> R.string.reading
            COMPLETED -> R.string.completed
            ON_HOLD -> R.string.on_hold
            DROPPED -> R.string.dropped
            PLAN_TO_WATCH -> R.string.ptw
            PLAN_TO_READ -> R.string.ptr
        }

    // Status colours come from the (dynamic) colour scheme, so they follow the wallpaper /
    // chosen palette. Each status also has its own icon, which carries the meaning.
    @Composable
    override fun primaryColor() = when (this) {
        WATCHING, READING -> MaterialTheme.colorScheme.primary
        PLAN_TO_WATCH, PLAN_TO_READ -> MaterialTheme.colorScheme.outline
        COMPLETED -> MaterialTheme.colorScheme.tertiary
        ON_HOLD -> MaterialTheme.colorScheme.secondary
        DROPPED -> MaterialTheme.colorScheme.error
    }

    @Composable
    override fun onPrimaryColor() = when (this) {
        WATCHING, READING -> MaterialTheme.colorScheme.onPrimary
        PLAN_TO_WATCH, PLAN_TO_READ -> MaterialTheme.colorScheme.surface
        COMPLETED -> MaterialTheme.colorScheme.onTertiary
        ON_HOLD -> MaterialTheme.colorScheme.onSecondary
        DROPPED -> MaterialTheme.colorScheme.onError
    }

    companion object {

        val listStatusAnimeValues = arrayOf(WATCHING, PLAN_TO_WATCH, COMPLETED, ON_HOLD, DROPPED)

        val listStatusMangaValues = arrayOf(READING, PLAN_TO_READ, COMPLETED, ON_HOLD, DROPPED)

        fun listStatusValues(mediaType: MediaType) =
            if (mediaType == MediaType.ANIME) listStatusAnimeValues else listStatusMangaValues

        val navType = object : NavType<ListStatus?>(isNullableAllowed = true) {
            override fun get(bundle: Bundle, key: String): ListStatus? {
                return try {
                    bundle.getString(key)?.let {
                        ListStatus.valueOf(it)
                    }
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            override fun parseValue(value: String): ListStatus? {
                if (value == "null") return null
                return try {
                    ListStatus.valueOf(value)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            override fun put(bundle: Bundle, key: String, value: ListStatus?) {
                bundle.putString(key, value?.name)
            }
        }
    }
}
