package com.example.wallhacks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallhacks.data.FakeWallhackerServer
import com.example.wallhacks.data.LocalizationResult
import com.example.wallhacks.data.WallhackerServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class WallhackerUiState(
    val isSending: Boolean = false,
    val result: LocalizationResult? = null,
    val error: String? = null,
)

/** Shared by the camera and map screens so the map shows whatever the last query returned. */
class WallhackerViewModel(
    private val server: WallhackerServer = FakeWallhackerServer(),
) : ViewModel() {

    private val _state = MutableStateFlow(WallhackerUiState())
    val state: StateFlow<WallhackerUiState> = _state.asStateFlow()

    fun onPhotoCaptured(photo: File) {
        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            try {
                val result = server.localize(photo, FakeWallhackerServer.mockExif())
                _state.update { it.copy(isSending = false, result = result) }
            } catch (e: Exception) {
                _state.update { it.copy(isSending = false, error = e.message ?: "Request failed") }
            }
        }
    }

    fun onCaptureFailed(message: String) {
        _state.update { it.copy(error = message) }
    }
}
