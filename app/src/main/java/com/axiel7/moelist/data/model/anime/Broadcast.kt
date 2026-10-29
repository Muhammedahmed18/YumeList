package com.axiel7.moelist.data.model.anime

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.utils.BroadcastSchedule
import com.axiel7.moelist.utils.DateUtils.secondsToLegibleText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * A MAL broadcast: weekday + start time, both in Japan time (JST).
 *
 * Everything shown to the user is converted to the phone's time zone through [BroadcastSchedule],
 * and times follow the phone's 12/24-hour setting.
 */
@Serializable
data class Broadcast(
    @SerialName("day_of_the_week")
    val dayOfTheWeek: WeekDay? = null,
    @SerialName("start_time")
    val startTime: String? = null
) {

    private val jpDay: DayOfWeek? get() = dayOfTheWeek?.let { DayOfWeek.of(it.ordinal + 1) }

    private val jpTime: LocalTime?
        get() = startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

    private fun now(): ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

    /**
     * Today's airing if there is one in the user's zone (even if it already aired), otherwise
     * the next one. Null when the day or time is unknown.
     */
    private fun relevantAiring(now: ZonedDateTime = now()): ZonedDateTime? {
        val day = jpDay ?: return null
        val time = jpTime ?: return null
        return BroadcastSchedule.relevantAiring(day, time, now)
    }

    /**
     * True if this broadcast airs on [date] in [zone] (the user's day, not Japan's).
     * Broadcasts with a day but no time can't be converted, so they fall back to Japan's day.
     */
    fun airsOn(
        date: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val day = jpDay ?: return false
        val time = jpTime
            ?: return day == BroadcastSchedule.japanToday(ZonedDateTime.now(zone))
        return BroadcastSchedule.airingOn(day, time, date, zone) != null
    }

    /** Local time this broadcast airs on [date], for sorting. Null if unknown. */
    fun localAiringOn(
        date: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): ZonedDateTime? {
        val day = jpDay ?: return null
        val time = jpTime ?: return null
        return BroadcastSchedule.airingOn(day, time, date, zone)
    }

    @Composable
    fun timeText(isAiring: Boolean) = buildString {
        val firstText = when {
            dayOfTheWeek != null && startTime != null -> {
                relevantAiring()?.format(formatter("EE"))
            }

            dayOfTheWeek != null -> dayOfTheWeek.localized()

            startTime != null -> "$startTime (JST)"

            else -> null
        }

        if (firstText != null) {
            append(firstText)

            if (isAiring) {
                val airingIn = airingInString()
                if (airingIn.isNotEmpty()) {
                    append("\n$airingIn")
                }
            }
        } else {
            append(stringResource(R.string.unknown))
        }
    }

    fun localStartTime() = runCatching {
        relevantAiring()?.format(formatter(""))
    }.getOrNull()

    fun localDayOfTheWeek(): DayOfWeek? = runCatching {
        relevantAiring()?.dayOfWeek
    }.getOrNull()

    fun localDayAndTimeText() = runCatching {
        val day = localDayOfTheWeek()?.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val time = localStartTime()
        when {
            day != null && time != null -> "$day, $time"
            day != null -> day
            time != null -> time
            else -> null
        }
    }.getOrNull()

    @Composable
    fun airingInString() = if (startTime != null && dayOfTheWeek != null) {
        val remaining = remaining()
        if (remaining > 0) {
            stringResource(R.string.airing_in).format(remaining.secondsToLegibleText())
        } else stringResource(R.string.aired_ago).format(remaining.absoluteValue.secondsToLegibleText())
    } else ""

    @Composable
    fun airingInWithTime() = buildString {
        append(airingInString())
        localStartTime()?.let {
            append(" ($it)")
        }
    }

    @Composable
    fun airingInWithDayAndTime() = buildString {
        append(airingInString())
        val day = localDayOfTheWeek()?.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val time = localStartTime()
        if (day != null || time != null) {
            append(" (")
            if (day != null) {
                append(day)
                if (time != null) append(", ")
            }
            if (time != null) append(time)
            append(")")
        }
    }

    @Composable
    fun airingInShortString() = if (startTime != null && dayOfTheWeek != null) {
        val remaining = remaining()
        if (remaining > 0) remaining.secondsToLegibleText()
        else stringResource(R.string.ago).format(remaining.absoluteValue.secondsToLegibleText())
    } else ""

    fun nextAiringDayFormatted() = runCatching {
        relevantAiring()?.format(formatter("EEdMMM"))
    }.getOrNull()

    /** Seconds until the relevant airing (negative if it aired earlier today). */
    private fun remaining(): Long {
        val now = now()
        val airing = relevantAiring(now) ?: return 0
        return BroadcastSchedule.secondsBetween(now, airing)
    }

    /** Epoch seconds of the relevant airing, for sorting. 0 if unknown. */
    fun secondsUntilNextBroadcast(): Long = relevantAiring()?.toEpochSecond() ?: 0

    private companion object {
        /**
         * A localized pattern for [dateSkeleton] plus the time, following the phone's
         * 12/24-hour setting (not just the language), e.g. "Thu 19:00" or "Thu 7:00 PM".
         */
        fun formatter(dateSkeleton: String): DateTimeFormatter {
            val is24Hour = runCatching { DateFormat.is24HourFormat(App.instance) }
                .getOrDefault(false)
            val locale = Locale.getDefault()
            return runCatching {
                DateTimeFormatter.ofPattern(
                    DateFormat.getBestDateTimePattern(
                        locale,
                        dateSkeleton + if (is24Hour) "Hm" else "hm"
                    ),
                    locale
                )
            }.getOrElse {
                // Some locales' patterns use letters java.time doesn't know; use a plain one
                val time = if (is24Hour) "HH:mm" else "h:mm a"
                val date = when (dateSkeleton) {
                    "" -> ""
                    "EE" -> "EEE "
                    else -> "EEE, d MMM "
                }
                DateTimeFormatter.ofPattern(date + time, locale)
            }
        }
    }
}
