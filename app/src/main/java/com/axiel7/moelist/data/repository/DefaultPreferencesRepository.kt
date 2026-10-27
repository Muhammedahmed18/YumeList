package com.axiel7.moelist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.axiel7.moelist.App
import com.axiel7.moelist.data.model.AccessToken
import com.axiel7.moelist.data.model.media.ListStatus
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.di.getValue
import com.axiel7.moelist.di.setValue
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.TabletMode
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.utils.NumExtensions.toInt
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val accessToken = dataStore.getValue(ACCESS_TOKEN_KEY)
    val refreshToken = dataStore.getValue(REFRESH_TOKEN_KEY)

    suspend fun saveTokens(value: AccessToken) {
        dataStore.edit {
            if (value.accessToken != null) it[ACCESS_TOKEN_KEY] = value.accessToken
            if (value.refreshToken != null) it[REFRESH_TOKEN_KEY] = value.refreshToken
        }
    }

    suspend fun removeTokens() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
        }
    }

    suspend fun clearUserData() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
            it.remove(PROFILE_PICTURE_KEY)
            it.remove(USERNAME_KEY)
        }
    }

    suspend fun saveCodeVerifier(value: String) {
        dataStore.setValue(CODE_VERIFIER_KEY, value)
    }

    suspend fun getCodeVerifier(): String? {
        return dataStore.getValue(CODE_VERIFIER_KEY).first()
    }

    suspend fun removeCodeVerifier() {
        dataStore.edit {
            it.remove(CODE_VERIFIER_KEY)
        }
    }

    val nsfw = dataStore.getValue(NSFW_KEY, false)
    suspend fun nsfwInt() = nsfw.first().toInt()
    suspend fun setNsfw(value: Boolean) {
        dataStore.setValue(NSFW_KEY, value)
    }

    val hideScores = dataStore.getValue(HIDE_SCORES_KEY, false)
    suspend fun setHideScores(value: Boolean) {
        dataStore.setValue(HIDE_SCORES_KEY, value)
    }

    /**
     * IDs of media entries for which the user explicitly skipped the
     * "set score on finish" reminder dialog. Once skipped, the reminder
     * should never be shown again for that entry.
     */
    val dismissedScoreReminderIds = dataStore.getValue(DISMISSED_SCORE_REMINDER_IDS_KEY, emptySet())

    suspend fun addDismissedScoreReminderId(mediaId: Int) {
        dataStore.edit { prefs ->
            val current = prefs[DISMISSED_SCORE_REMINDER_IDS_KEY] ?: emptySet()
            prefs[DISMISSED_SCORE_REMINDER_IDS_KEY] = current + mediaId.toString()
        }
    }

    /** The theme name as saved: one of [ThemeStyle], or [LEGACY_AMOLED] from older versions. */
    private val savedThemeName = dataStore.getValue(THEME_KEY, ThemeStyle.FOLLOW_SYSTEM.name)

    /**
     * Theme mode: System / Light / Dark. The old AMOLED mode is now Dark + [pureBlack], so a saved
     * "AMOLED" reads as DARK here (no migration step needed).
     */
    val theme = savedThemeName.map {
        if (it == LEGACY_AMOLED) ThemeStyle.DARK
        else ThemeStyle.valueOfOrNull(it) ?: ThemeStyle.FOLLOW_SYSTEM
    }

    suspend fun setTheme(value: ThemeStyle) {
        dataStore.edit { prefs ->
            // Keep pure black for someone moving off the old AMOLED mode
            if (prefs[THEME_KEY] == LEGACY_AMOLED && prefs[PURE_BLACK_KEY] == null) {
                prefs[PURE_BLACK_KEY] = true
            }
            prefs[THEME_KEY] = value.name
        }
    }

    /**
     * Pure black backgrounds whenever the app is dark (Dark mode, or System at night).
     * Never set yet -> on if the old AMOLED mode was saved.
     */
    val pureBlack = combine(savedThemeName, dataStore.getValue(PURE_BLACK_KEY)) { saved, pureBlack ->
        pureBlack ?: (saved == LEGACY_AMOLED)
    }

    suspend fun setPureBlack(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PURE_BLACK_KEY] = value
            if (prefs[THEME_KEY] == LEGACY_AMOLED) prefs[THEME_KEY] = ThemeStyle.DARK.name
        }
    }

    val colorPalette = dataStore.getValue(COLOR_PALETTE_KEY, ColorPalette.DYNAMIC.name)
        .map { ColorPalette.valueOfOrNull(it) ?: ColorPalette.DYNAMIC }
    suspend fun setColorPalette(value: ColorPalette) {
        dataStore.setValue(COLOR_PALETTE_KEY, value.name)
    }

    val isOnboardingCompleted = dataStore.getValue(ONBOARDING_COMPLETED_KEY, false)
    suspend fun setOnboardingCompleted(value: Boolean) {
        dataStore.setValue(ONBOARDING_COMPLETED_KEY, value)
    }

    val lastTab = dataStore.getValue(LAST_TAB_KEY, 0)
    suspend fun setLastTab(value: Int) {
        dataStore.setValue(LAST_TAB_KEY, value)
    }

    val pinnedNavBar = dataStore.getValue(PINNED_NAV_BAR_KEY, false)
    suspend fun setPinnedNavBar(value: Boolean) {
        dataStore.setValue(PINNED_NAV_BAR_KEY, value)
    }

    val profilePicture = dataStore.getValue(PROFILE_PICTURE_KEY)
    suspend fun setProfilePicture(value: String) {
        dataStore.setValue(PROFILE_PICTURE_KEY, value)
    }

    val username = dataStore.getValue(USERNAME_KEY)
    suspend fun setUsername(value: String) {
        dataStore.setValue(USERNAME_KEY, value)
    }

    val animeListStatus = dataStore.getValue(ANIME_LIST_STATUS_KEY, ListStatus.WATCHING.name)
        .map { ListStatus.valueOf(it) }

    suspend fun setAnimeListStatus(value: ListStatus) {
        dataStore.setValue(ANIME_LIST_STATUS_KEY, value.name)
    }

    val mangaListStatus = dataStore.getValue(MANGA_LIST_STATUS_KEY, ListStatus.READING.name)
        .map { ListStatus.valueOf(it) }

    suspend fun setMangaListStatus(value: ListStatus) {
        dataStore.setValue(MANGA_LIST_STATUS_KEY, value.name)
    }

    val animeListSort = dataStore.getValue(ANIME_LIST_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeListSort(value: MediaSort) {
        dataStore.setValue(ANIME_LIST_SORT_KEY, value.value)
    }

    val mangaListSort = dataStore.getValue(MANGA_LIST_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaListSort(value: MediaSort) {
        dataStore.setValue(MANGA_LIST_SORT_KEY, value.value)
    }

    val animeWatchingSort = dataStore.getValue(ANIME_WATCHING_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeWatchingSort(value: MediaSort) {
        dataStore.setValue(ANIME_WATCHING_SORT_KEY, value.value)
    }

    val animePlannedSort = dataStore.getValue(ANIME_PLANNED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimePlannedSort(value: MediaSort) {
        dataStore.setValue(ANIME_PLANNED_SORT_KEY, value.value)
    }

    val animeCompletedSort = dataStore.getValue(ANIME_COMPLETED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeCompletedSort(value: MediaSort) {
        dataStore.setValue(ANIME_COMPLETED_SORT_KEY, value.value)
    }

    val animePausedSort = dataStore.getValue(ANIME_PAUSED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimePausedSort(value: MediaSort) {
        dataStore.setValue(ANIME_PAUSED_SORT_KEY, value.value)
    }

    val animeDroppedSort = dataStore.getValue(ANIME_DROPPED_SORT_KEY, MediaSort.ANIME_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.ANIME_TITLE }

    suspend fun setAnimeDroppedSort(value: MediaSort) {
        dataStore.setValue(ANIME_DROPPED_SORT_KEY, value.value)
    }

    val mangaReadingSort = dataStore.getValue(MANGA_READING_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaReadingSort(value: MediaSort) {
        dataStore.setValue(MANGA_READING_SORT_KEY, value.value)
    }

    val mangaPlannedSort = dataStore.getValue(MANGA_PLANNED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaPlannedSort(value: MediaSort) {
        dataStore.setValue(MANGA_PLANNED_SORT_KEY, value.value)
    }

    val mangaCompletedSort = dataStore.getValue(MANGA_COMPLETED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaCompletedSort(value: MediaSort) {
        dataStore.setValue(MANGA_COMPLETED_SORT_KEY, value.value)
    }

    val mangaPausedSort = dataStore.getValue(MANGA_PAUSED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaPausedSort(value: MediaSort) {
        dataStore.setValue(MANGA_PAUSED_SORT_KEY, value.value)
    }

    val mangaDroppedSort = dataStore.getValue(MANGA_DROPPED_SORT_KEY, MediaSort.MANGA_TITLE.value)
        .map { MediaSort.valueOf(malValue = it) ?: MediaSort.MANGA_TITLE }

    suspend fun setMangaDroppedSort(value: MediaSort) {
        dataStore.setValue(MANGA_DROPPED_SORT_KEY, value.value)
    }

    val startTab = dataStore.getValue(START_TAB_KEY, StartTab.LAST_USED.value)
        .map { StartTab.valueOf(tabName = it) }
    suspend fun setStartTab(value: StartTab) {
        dataStore.setValue(START_TAB_KEY, value.value)
    }

    val tabletMode = dataStore.getValue(TABLET_MODE_KEY, TabletMode.AUTO.name)
        .map { TabletMode.valueOf(it) }
    suspend fun setTabletMode(value: TabletMode) {
        dataStore.setValue(TABLET_MODE_KEY, value.name)
    }

    val titleLang = dataStore.getValue(TITLE_LANG_KEY, TitleLanguage.ROMAJI.name)
        .map { TitleLanguage.valueOf(it) }

    suspend fun setTitleLang(value: TitleLanguage) {
        dataStore.setValue(TITLE_LANG_KEY, value.name)
        App.titleLanguage = value
    }

    val useListTabs = dataStore.getValue(USE_LIST_TABS_KEY, false)
    suspend fun setUseListTabs(value: Boolean) {
        dataStore.setValue(USE_LIST_TABS_KEY, value)
    }

    val loadCharacters = dataStore.getValue(LOAD_CHARACTERS_KEY, false)
    suspend fun setLoadCharacters(value: Boolean) {
        dataStore.setValue(LOAD_CHARACTERS_KEY, value)
    }

    val randomListEntryEnabled = dataStore.getValue(RANDOM_LIST_ENTRY_KEY, false)
    suspend fun setRandomListEntryEnabled(value: Boolean) {
        dataStore.setValue(RANDOM_LIST_ENTRY_KEY, value)
    }

    companion object {

        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val CODE_VERIFIER_KEY = stringPreferencesKey("code_verifier")

        private val NSFW_KEY = booleanPreferencesKey("nsfw")
        private val HIDE_SCORES_KEY = booleanPreferencesKey("hide_scores")
        private val DISMISSED_SCORE_REMINDER_IDS_KEY =
            stringSetPreferencesKey("dismissed_score_reminder_ids")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val LAST_TAB_KEY = intPreferencesKey("last_tab")
        private val PINNED_NAV_BAR_KEY = booleanPreferencesKey("pinned_nav_bar")
        private val PURE_BLACK_KEY = booleanPreferencesKey("pure_black")

        /** The removed AMOLED theme as older versions saved it (now DARK + pure black). */
        private const val LEGACY_AMOLED = "AMOLED"
        private val PROFILE_PICTURE_KEY = stringPreferencesKey("profile_picture")
        private val USERNAME_KEY = stringPreferencesKey("username")

        private val ANIME_LIST_STATUS_KEY = stringPreferencesKey("anime_list_status")
        private val MANGA_LIST_STATUS_KEY = stringPreferencesKey("manga_list_status")
        private val ANIME_LIST_SORT_KEY = stringPreferencesKey("anime_list_sort")
        private val MANGA_LIST_SORT_KEY = stringPreferencesKey("manga_list_sort")
        private val ANIME_WATCHING_SORT_KEY = stringPreferencesKey("anime_watching_sort")
        private val ANIME_PLANNED_SORT_KEY = stringPreferencesKey("anime_planned_sort")
        private val ANIME_COMPLETED_SORT_KEY = stringPreferencesKey("anime_completed_sort")
        private val ANIME_PAUSED_SORT_KEY = stringPreferencesKey("anime_paused_sort")
        private val ANIME_DROPPED_SORT_KEY = stringPreferencesKey("anime_dropped_sort")
        private val MANGA_READING_SORT_KEY = stringPreferencesKey("manga_reading_sort")
        private val MANGA_PLANNED_SORT_KEY = stringPreferencesKey("manga_planned_sort")
        private val MANGA_COMPLETED_SORT_KEY = stringPreferencesKey("manga_completed_sort")
        private val MANGA_PAUSED_SORT_KEY = stringPreferencesKey("manga_paused_sort")
        private val MANGA_DROPPED_SORT_KEY = stringPreferencesKey("manga_dropped_sort")

        private val START_TAB_KEY = stringPreferencesKey("start_tab")
        private val TABLET_MODE_KEY = stringPreferencesKey("tablet_mode")
        private val TITLE_LANG_KEY = stringPreferencesKey("title_lang")
        private val USE_LIST_TABS_KEY = booleanPreferencesKey("use_list_tabs")
        private val LOAD_CHARACTERS_KEY = booleanPreferencesKey("load_characters")
        private val RANDOM_LIST_ENTRY_KEY = booleanPreferencesKey("random_list_entry_enabled")
    }
}