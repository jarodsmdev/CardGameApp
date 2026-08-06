package com.jarod.card.features.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarod.card.core.util.DispatchersProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LobbyUiState(
    val dispatcherLabel: String = ""
)

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val dispatchers: DispatchersProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = LobbyUiState(
                dispatcherLabel = dispatchers.io.toString()
            )
        }
    }
}
