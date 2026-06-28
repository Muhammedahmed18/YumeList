package com.axiel7.moelist.ui.more.settings.content

import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.event.UiEvent

interface ContentEvent : UiEvent {
    fun setTitleLanguage(value: TitleLanguage)
    fun setShowNsfw(value: Boolean)
    fun setHideScores(value: Boolean)
    fun setLoadCharacters(value: Boolean)
    fun setRandomListEntryEnabled(value: Boolean)
}
