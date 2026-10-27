package com.axiel7.moelist.ui.details

import com.axiel7.moelist.data.model.media.BaseMyListStatus
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.WeekDay
import com.axiel7.moelist.ui.base.event.UiEvent
import java.time.LocalDate
import java.time.LocalTime

interface MediaDetailsEvent : UiEvent {

    fun onChangedMyListStatus(value: BaseMyListStatus?, removed: Boolean = false)

    /**
     * Quick status change from the details split button (no edit sheet). Applies the same
     * rules as the edit sheet: Completed fills in the total progress and today's finish date,
     * starting a planned or new entry sets today's start date.
     */
    fun setListStatus(status: ListStatus)

    fun getCharacters()

    fun scheduleAiringAnimeNotification(
        title: String,
        animeId: Int,
        weekDay: WeekDay,
        jpHour: LocalTime
    )

    fun scheduleAnimeStartNotification(
        title: String,
        animeId: Int,
        startDate: LocalDate,
    )

    fun removeAiringAnimeNotification(animeId: Int)
}