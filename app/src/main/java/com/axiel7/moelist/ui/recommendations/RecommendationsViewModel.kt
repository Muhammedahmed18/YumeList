package com.axiel7.moelist.ui.recommendations

import androidx.lifecycle.viewModelScope
import com.axiel7.moelist.data.model.media.MediaType
import com.axiel7.moelist.data.model.recommendation.RecommendedMedia
import com.axiel7.moelist.data.repository.DefaultPreferencesRepository
import com.axiel7.moelist.data.repository.RecommendationRepository
import com.axiel7.moelist.ui.base.viewmodel.BaseViewModel
import com.axiel7.moelist.ui.recommendations.composables.RecommendationActionResult
import com.axiel7.moelist.utils.toUiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecommendationsViewModel(
    mediaType: MediaType,
    private val recommendationRepository: RecommendationRepository,
    defaultPreferencesRepository: DefaultPreferencesRepository,
) : BaseViewModel<RecommendationsUiState>(), RecommendationsEvent {

    override val mutableUiState = MutableStateFlow(RecommendationsUiState(mediaType = mediaType))

    private val mediaType get() = mutableUiState.value.mediaType
    private var loadJob: Job? = null
    private var loadedForLoggedIn: Boolean? = null

    override fun load(isLoggedIn: Boolean) {
        if (loadedForLoggedIn == isLoggedIn && (loadJob?.isActive == true || uiState.value.hasContent)) return
        val loginChanged = loadedForLoggedIn != null && loadedForLoggedIn != isLoggedIn
        loadedForLoggedIn = isLoggedIn
        if (loginChanged) mutableUiState.update { it.copy(result = null) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            // Show the saved result straight away, then bring it up to date
            if (uiState.value.result == null) {
                recommendationRepository.peekForYou(mediaType, isLoggedIn)?.let { cached ->
                    mutableUiState.update { it.copy(result = cached, isLoading = false) }
                }
            }
            fetch(isLoggedIn, forceRefresh = false)
        }
    }

    override fun refresh(isLoggedIn: Boolean) {
        loadedForLoggedIn = isLoggedIn
        loadJob?.cancel()
        mutableUiState.update {
            it.copy(isRefreshing = it.hasContent, isLoading = !it.hasContent, message = null)
        }
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            fetch(isLoggedIn, forceRefresh = true)
        }
    }

    private suspend fun fetch(isLoggedIn: Boolean, forceRefresh: Boolean) {
        val response = recommendationRepository.getForYou(mediaType, isLoggedIn, forceRefresh)
        val data = response.data
        if (data != null) {
            mutableUiState.update {
                it.copy(result = data, isLoading = false, isRefreshing = false)
            }
        } else {
            mutableUiState.update { it.copy(isLoading = false, isRefreshing = false) }
            showMessage(response.message ?: response.error)
        }
    }

    override fun addToPlan(item: RecommendedMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            val error = recommendationRepository.addToPlan(item)
            mutableUiState.update {
                it.copy(
                    actionResult = if (error == null) {
                        RecommendationActionResult(RecommendationActionResult.Kind.ADDED_TO_PLAN, item)
                    } else {
                        RecommendationActionResult(
                            kind = RecommendationActionResult.Kind.FAILED,
                            item = item,
                            errorMessage = error.toUiMessage(),
                        )
                    }
                )
            }
        }
    }

    override fun notInterested(item: RecommendedMedia) {
        mutableUiState.update {
            it.copy(
                result = it.result?.without(setOf(item.id)),
                actionResult = RecommendationActionResult(RecommendationActionResult.Kind.HIDDEN, item),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            recommendationRepository.hide(mediaType, item.id)
        }
    }

    override fun undoNotInterested(item: RecommendedMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            recommendationRepository.unhide(mediaType, item.id)
            // The saved result still has it: reading it again puts it back in place
            val isLoggedIn = loadedForLoggedIn ?: return@launch
            recommendationRepository.peekForYou(mediaType, isLoggedIn)?.let { restored ->
                mutableUiState.update { it.copy(result = restored) }
            }
        }
    }

    override fun onActionResultShown() {
        mutableUiState.update { it.copy(actionResult = null) }
    }

    init {
        defaultPreferencesRepository.hideScores
            .onEach { value -> mutableUiState.update { it.copy(hideScore = value) } }
            .launchIn(viewModelScope)

        recommendationRepository.listStatuses(mediaType)
            .onEach { statuses -> mutableUiState.update { it.copy(statuses = statuses) } }
            .launchIn(viewModelScope)
    }
}
