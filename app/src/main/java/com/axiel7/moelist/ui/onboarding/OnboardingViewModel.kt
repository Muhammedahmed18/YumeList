package com.axiel7.moelist.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.MediaSort
import com.axiel7.moelist.data.model.media.TitleLanguage
import com.axiel7.moelist.data.repository.AnimeRepository
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.UserRepository
import com.axiel7.moelist.ui.base.ColorPalette
import com.axiel7.moelist.ui.base.StartTab
import com.axiel7.moelist.ui.base.ThemeStyle
import com.axiel7.moelist.utils.SeasonCalendar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One row of the "Bringing your list over" card. */
enum class ImportRowStatus { WAITING, LOADING, DONE, FAILED }

data class ImportRow(
    val status: ImportRowStatus = ImportRowStatus.WAITING,
    val value: Int = 0,
)

/** Anime numbers from the MAL profile, shown as chips once everything is in. */
data class QuickStats(
    val daysWatched: Float,
    val episodes: Int,
    val meanScore: Float,
    val completed: Int,
)

/** A title from the user's own list, used to show what each title language looks like. */
data class TitleSample(
    val romaji: String,
    val english: String,
    val japanese: String,
)

data class ImportState(
    val animeList: ImportRow = ImportRow(),
    val mangaList: ImportRow = ImportRow(),
    val watching: ImportRow = ImportRow(),
    val airing: ImportRow = ImportRow(),
    val stats: QuickStats? = null,
    /** Cover of the user's best-scored / currently watching anime, for the blurred header. */
    val heroImage: String? = null,
    val isFinished: Boolean = false,
)

/**
 * Data and settings for the onboarding flow. Session and login stay in [MainViewModel]
 * (the login redirect lands on the activity's instance).
 */
class OnboardingViewModel(
    private val animeRepository: AnimeRepository,
    private val userRepository: UserRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
) : ViewModel() {

    // ── Login: cover collage ──────────────────────────────────────────────────

    private val _covers = MutableStateFlow<List<String>>(emptyList())

    /** This season's most popular covers (MAL works with the app's client id, no login needed). */
    val covers = _covers.asStateFlow()

    private fun loadCovers() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) {
            animeRepository.getSeasonalAnimes(
                sort = MediaSort.ANIME_NUM_USERS,
                startSeason = SeasonCalendar.currentStartSeason,
                limit = 36,
                fields = "main_picture",
            )
        }
        _covers.value = result.data
            ?.mapNotNull { it.node.mainPicture?.large ?: it.node.mainPicture?.medium }
            ?.distinct()
            .orEmpty()
    }

    // ── Stats: importing the account ─────────────────────────────────────────

    private val _importState = MutableStateFlow(ImportState())
    val importState = _importState.asStateFlow()

    private val _titleSample = MutableStateFlow<TitleSample?>(null)
    val titleSample = _titleSample.asStateFlow()

    private var importStarted = false

    /** Loads the account summary once. Each row finishes on its own, so the card fills in live. */
    fun startImport() {
        if (importStarted) return
        importStarted = true
        viewModelScope.launch {
            val user = async(Dispatchers.IO) { userRepository.getMyUser() }
            val manga = async(Dispatchers.IO) { userRepository.getMyMangaStats() }
            val airing = async(Dispatchers.IO) { animeRepository.getAiringAnimeOnList() }

            _importState.update {
                it.copy(
                    animeList = ImportRow(ImportRowStatus.LOADING),
                    mangaList = ImportRow(ImportRowStatus.LOADING),
                    watching = ImportRow(ImportRowStatus.LOADING),
                    airing = ImportRow(ImportRowStatus.LOADING),
                )
            }

            // Anime list + watching come from the profile's anime statistics
            val profile = runCatchingNonCancel { user.await() }
            val animeStats = profile?.animeStatistics
            profile?.picture?.let { defaultPreferencesRepository.setProfilePicture(it) }
            profile?.name?.let { defaultPreferencesRepository.setUsername(it) }
            _importState.update {
                it.copy(
                    animeList = rowOf(animeStats?.numItems),
                    stats = animeStats?.let { s ->
                        QuickStats(
                            daysWatched = s.numDaysWatched ?: 0f,
                            episodes = s.numEpisodes ?: 0,
                            meanScore = s.meanScore ?: 0f,
                            completed = s.numItemsCompleted ?: 0,
                        )
                    },
                )
            }
            stagger()
            _importState.update { it.copy(watching = rowOf(animeStats?.numItemsWatching)) }

            stagger()
            val mangaResult = runCatchingNonCancel { manga.await() }
            _importState.update { it.copy(mangaList = rowOf(mangaResult?.data?.totalEntries)) }

            stagger()
            val airingList = runCatchingNonCancel { airing.await() }
            _importState.update {
                it.copy(
                    airing = rowOf(airingList?.size),
                    heroImage = airingList?.firstNotNullOfOrNull { node ->
                        node.mainPicture?.large ?: node.mainPicture?.medium
                    },
                )
            }
            // A real title from their list for the title-language picker, when it has all three
            airingList
                ?.firstOrNull { node ->
                    !node.alternativeTitles?.en.isNullOrBlank() &&
                        !node.alternativeTitles?.ja.isNullOrBlank() &&
                        node.alternativeTitles?.en != node.title
                }
                ?.let { node ->
                    _titleSample.value = TitleSample(
                        romaji = node.title,
                        english = node.title(TitleLanguage.ENGLISH),
                        japanese = node.title(TitleLanguage.JAPANESE),
                    )
                }

            stagger()
            _importState.update { it.copy(isFinished = true) }
        }
    }

    private fun rowOf(value: Int?) =
        if (value == null) ImportRow(ImportRowStatus.FAILED)
        else ImportRow(ImportRowStatus.DONE, value)

    /** A short beat between rows, so they visibly tick off one by one. */
    private suspend fun stagger() = delay(260)

    private suspend fun <T> runCatchingNonCancel(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    // ── Make it yours: settings ──────────────────────────────────────────────

    val theme = defaultPreferencesRepository.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeStyle.FOLLOW_SYSTEM)
    val pureBlack = defaultPreferencesRepository.pureBlack
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val colorPalette = defaultPreferencesRepository.colorPalette
        .stateIn(viewModelScope, SharingStarted.Eagerly, ColorPalette.DYNAMIC)
    val startTab = defaultPreferencesRepository.startTab
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, StartTab.LAST_USED)
    val titleLanguage = defaultPreferencesRepository.titleLang
        .stateIn(viewModelScope, SharingStarted.Eagerly, TitleLanguage.ROMAJI)
    val useListTabs = defaultPreferencesRepository.useListTabs
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setTheme(value: ThemeStyle) = viewModelScope.launch {
        defaultPreferencesRepository.setTheme(value)
    }

    fun setPureBlack(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setPureBlack(value)
    }

    fun setColorPalette(value: ColorPalette) = viewModelScope.launch {
        defaultPreferencesRepository.setColorPalette(value)
    }

    fun setStartTab(value: StartTab) = viewModelScope.launch {
        defaultPreferencesRepository.setStartTab(value)
    }

    fun setTitleLanguage(value: TitleLanguage) = viewModelScope.launch {
        defaultPreferencesRepository.setTitleLang(value)
    }

    fun setUseListTabs(value: Boolean) = viewModelScope.launch {
        defaultPreferencesRepository.setUseListTabs(value)
    }

    init {
        loadCovers()
    }
}
