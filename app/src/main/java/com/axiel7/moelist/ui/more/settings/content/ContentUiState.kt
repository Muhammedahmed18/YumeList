package com.axiel7.moelist.ui.more.settings.content

import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.ui.base.state.UiState

data class ContentUiState(
    val titleLanguage: TitleLanguage = TitleLanguage.ROMAJI,
    val showNsfw: Boolean = false,
    val hideScores: Boolean = false,
    val loadCharacters: Boolean = false,
    val randomListEntryEnabled: Boolean = false,
    override val isLoading: Boolean = false,
    override val message: String? = null,
) : UiState() {
    override fun setLoading(value: Boolean) = copy(isLoading = value)
    override fun setMessage(value: String?) = copy(message = value)
}
