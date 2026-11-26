package mx.edu.utez.memorias.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utez.memorias.data.local.UserPreferencesRepository
import mx.edu.utez.memorias.data.local.entity.AvistamientoEntity
import mx.edu.utez.memorias.data.local.entity.EditHistoryEntity
import mx.edu.utez.memorias.data.repository.AvistamientoRepository

data class AvistamientoDetailUiState(
    val avistamiento: AvistamientoEntity? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isOwner: Boolean = false,
    val editHistory: List<EditHistoryEntity> = emptyList(),
    val showHistoryDialog: Boolean = false
)

sealed interface AvistamientoDetailEvent {
    data object DeletedAndNavigateBack : AvistamientoDetailEvent
    data class ShowError(val message: String) : AvistamientoDetailEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class AvistamientoDetailViewModel(
    private val avistamientoRepository: AvistamientoRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _avistamientoId = MutableStateFlow<Int?>(null)
    private val _showHistoryDialog = MutableStateFlow(false)

    private val _events = MutableSharedFlow<AvistamientoDetailEvent>()
    val events: SharedFlow<AvistamientoDetailEvent> = _events.asSharedFlow()

    val uiState: StateFlow<AvistamientoDetailUiState> = combine(
        _avistamientoId.filterNotNull(),
        preferencesRepository.userSessionFlow,
        _showHistoryDialog
    ) { id, session, showDialog ->
        Triple(id, session.matricula, showDialog)
    }.flatMapLatest { (id, currentMatricula, showDialog) ->
        combine(
            avistamientoRepository.observeAvistamientoById(id),
            avistamientoRepository.observeLikeCount(id),
            avistamientoRepository.observeIsLikedByUser(id, currentMatricula),
            avistamientoRepository.observeEditHistory(id)
        ) { avistamiento, likesCount, isLiked, history ->
            AvistamientoDetailUiState(
                avistamiento = avistamiento,
                likesCount = likesCount,
                isLikedByMe = isLiked,
                isOwner = avistamiento?.userMatricula == currentMatricula,
                editHistory = history,
                showHistoryDialog = showDialog
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AvistamientoDetailUiState())

    fun loadAvistamiento(avistamientoId: Int) {
        _avistamientoId.value = avistamientoId
        viewModelScope.launch {
            avistamientoRepository.refreshEditHistory(avistamientoId)
        }
    }

    fun toggleLike() {
        val avistamiento = uiState.value.avistamiento ?: return
        if (avistamiento.isDeleted == 1) return
        viewModelScope.launch {
            val session = preferencesRepository.userSessionFlow.first()
            avistamientoRepository.toggleLike(avistamiento.id, session.matricula)
        }
    }

    fun logicalDelete() {
        val avistamiento = uiState.value.avistamiento ?: return
        viewModelScope.launch {
            val session = preferencesRepository.userSessionFlow.first()
            avistamientoRepository.logicalDeleteAvistamiento(avistamiento.id, session.matricula)
                .onSuccess { _events.emit(AvistamientoDetailEvent.DeletedAndNavigateBack) }
                .onFailure { _events.emit(AvistamientoDetailEvent.ShowError(it.message ?: "Error al eliminar")) }
        }
    }

    fun setHistoryDialogVisible(visible: Boolean) {
        _showHistoryDialog.value = visible
    }
}
