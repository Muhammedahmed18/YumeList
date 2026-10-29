package com.axiel7.moelist.utils

import com.axiel7.moelist.data.model.anime.Season
import com.axiel7.moelist.data.model.anime.StartSeason
import com.axiel7.moelist.data.model.media.WeekDay
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object SeasonCalendar {
    private val calendar: Calendar by lazy {
        Calendar.getInstance(Locale.getDefault())
    }

    /**
     * The current month from 0 to 11
     */
    private val month = calendar[Calendar.MONTH]

    val currentYear = calendar[Calendar.YEAR]

    /**
     * The current anime season, using the standard anime/MAL season boundaries:
     * Winter = Jan–Mar, Spring = Apr–Jun, Summer = Jul–Sep, Fall = Oct–Dec.
     * (Previously Dec/Mar/Jun/Sep were already counted as the next season.)
     */
    val currentSeason = when (month) {
        0, 1, 2 -> Season.WINTER
        3, 4, 5 -> Season.SPRING
        6, 7, 8 -> Season.SUMMER
        9, 10, 11 -> Season.FALL
        else -> Season.WINTER
    }

    private val nextSeason = when (currentSeason) {
        Season.WINTER -> Season.SPRING
        Season.SPRING -> Season.SUMMER
        Season.SUMMER -> Season.FALL
        Season.FALL -> Season.WINTER
    }

    private val prevSeason = when (currentSeason) {
        Season.WINTER -> Season.FALL
        Season.SPRING -> Season.WINTER
        Season.SUMMER -> Season.SPRING
        Season.FALL -> Season.SUMMER
    }

    // Every season now sits inside its calendar year, so the current season is always this year
    val currentStartSeason = StartSeason(
        year = currentYear,
        season = currentSeason
    )

    // After Fall (Oct–Dec) comes next year's Winter
    val nextStartSeason = StartSeason(
        year = if (currentSeason == Season.FALL) currentYear + 1 else currentYear,
        season = nextSeason
    )

    // Before Winter (Jan–Mar) comes last year's Fall
    val prevStartSeason = StartSeason(
        year = if (currentSeason == Season.WINTER) currentYear - 1 else currentYear,
        season = prevSeason
    )

    /** Today's weekday in the user's time zone, worked out every time (not once at app start). */
    val currentWeekday: WeekDay
        get() = when (Calendar.getInstance(Locale.getDefault())[Calendar.DAY_OF_WEEK]) {
        2 -> WeekDay.MONDAY
        3 -> WeekDay.TUESDAY
        4 -> WeekDay.WEDNESDAY
        5 -> WeekDay.THURSDAY
        6 -> WeekDay.FRIDAY
        7 -> WeekDay.SATURDAY
        1 -> WeekDay.SUNDAY
        else -> WeekDay.MONDAY
    }

    /** Today's weekday in Japan, worked out every time. */
    val currentJapanWeekday: WeekDay
        get() = when (
            Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.ENGLISH)[Calendar.DAY_OF_WEEK]
        ) {
        2 -> WeekDay.MONDAY
        3 -> WeekDay.TUESDAY
        4 -> WeekDay.WEDNESDAY
        5 -> WeekDay.THURSDAY
        6 -> WeekDay.FRIDAY
        7 -> WeekDay.SATURDAY
        1 -> WeekDay.SUNDAY
        else -> WeekDay.MONDAY
    }

    val japanZoneId: ZoneId = ZoneId.of("Asia/Tokyo")
}