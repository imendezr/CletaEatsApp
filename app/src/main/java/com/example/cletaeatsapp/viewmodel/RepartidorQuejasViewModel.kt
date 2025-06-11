package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepartidorQuejasViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<RepartidorQuejasUiState>(RepartidorQuejasUiState.Loading)
    val uiState: StateFlow<RepartidorQuejasUiState> = _uiState.asStateFlow()

    init {
        loadQuejas()
    }

    private fun loadQuejas() {
        viewModelScope.launch {
            _uiState.value = RepartidorQuejasUiState.Loading
            try {
                val repartidores = repository.getRepartidores().map { repartidor ->
                    RepartidorQuejasData(
                        id = repartidor.id,
                        name = repartidor.nombre,
                        quejas = repartidor.quejas,
                        amonestaciones = repartidor.amonestaciones
                    )
                }
                _uiState.value = RepartidorQuejasUiState.Success(repartidores = repartidores)
            } catch (e: Exception) {
                _uiState.value = RepartidorQuejasUiState.Error("Error loading quejas: ${e.message}")
            }
        }
    }
}

data class RepartidorQuejasData(
    val id: String,
    val name: String,
    val quejas: List<String>,
    val amonestaciones: Int
)

sealed class RepartidorQuejasUiState {
    object Loading : RepartidorQuejasUiState()
    data class Success(val repartidores: List<RepartidorQuejasData>) : RepartidorQuejasUiState()
    data class Error(val message: String) : RepartidorQuejasUiState()
}
