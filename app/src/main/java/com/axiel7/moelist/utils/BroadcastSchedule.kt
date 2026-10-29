package com.axiel7.moelist.utils

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Turns a MyAnimeList broadcast (a weekday + time in Japan) into real moments in the user's
 * time zone.
 *
 * MAL only gives Japan time (JST). The same broadcast falls on a different local day depending
 * on where the user is: "Thursday 01:30 JST" is Wednesday evening in Pakistan, Wednesday morning
 * in New York and still Thursday in Japan. Every "which day" / "how long until" decision in the
 * app must go through here, so it's made in the user's zone and never mixes Japan's day with the
 * user's date.
 *
 * Plain java.time (no Android), so it's unit tested across many zones with a fixed clock.
 */
object BroadcastSchedule {

    val JAPAN: ZoneId = ZoneId.of("Asia/Tokyo")

    /**
     * The airing that lands on [date] in [zone], or null if this broadcast doesn't air that day.
     *
     * Any zone is between 21h behind and 5h ahead of Japan, so an airing on local date D always
     * comes from a Japan date in D-1..D+1. Those are three consecutive days, so at most one of
     * them has the right weekday.
     */
    fun airingOn(
        day: DayOfWeek,
        jpTime: LocalTime,
        date: LocalDate,
        zone: ZoneId,
    ): ZonedDateTime? = (-1L..1L)
        .map { date.plusDays(it) }
        .filter { it.dayOfWeek == day }
        .map { ZonedDateTime.of(it, jpTime, JAPAN).withZoneSameInstant(zone) }
        .firstOrNull { it.toLocalDate() == date }

    /** The next airing at or after [now], in [now]'s zone. */
    fun nextAiring(
        day: DayOfWeek,
        jpTime: LocalTime,
        now: ZonedDateTime,
    ): ZonedDateTime {
        val nowInJapan = now.withZoneSameInstant(JAPAN)
        var airing = ZonedDateTime.of(
            nowInJapan.toLocalDate().with(TemporalAdjusters.nextOrSame(day)),
            jpTime,
            JAPAN,
        )
        if (airing.isBefore(nowInJapan)) airing = airing.plusWeeks(1)
        return airing.withZoneSameInstant(now.zone)
    }

    /**
     * The airing to show the user right now: today's (local) airing if there is one, even if it
     * already aired (so it can say "aired 2h ago"), otherwise the next one.
     */
    fun relevantAiring(
        day: DayOfWeek,
        jpTime: LocalTime,
        now: ZonedDateTime,
    ): ZonedDateTime = airingOn(day, jpTime, now.toLocalDate(), now.zone)
        ?: nextAiring(day, jpTime, now)

    /**
     * Seconds from [now] until [airing] (negative once it has aired). Uses real instants, so
     * daylight saving changes in the user's zone are handled correctly.
     */
    fun secondsBetween(now: ZonedDateTime, airing: ZonedDateTime): Long =
        Duration.between(now.toInstant(), airing.toInstant()).seconds

    /** For broadcasts with a day but no time: today's weekday in Japan (best effort). */
    fun japanToday(now: ZonedDateTime): DayOfWeek =
        now.withZoneSameInstant(JAPAN).dayOfWeek
}
