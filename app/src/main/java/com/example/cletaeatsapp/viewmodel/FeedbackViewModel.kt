package com.example.cletaeatsapp.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var rating = mutableIntStateOf(0)
    var comentario = mutableStateOf("")
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun submitFeedback(repartidorId: String?, onSuccess: () -> Unit) {
        if (repartidorId == null) {
            _errorMessage.value = "Error: Repartidor no asignado"
            return
        }
        if (rating.intValue == 0) {
            _errorMessage.value = "Por favor seleccione una calificación"
            return
        }
        if (comentario.value.length > 500) {
            _errorMessage.value = "El comentario no puede exceder los 500 caracteres"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.addQueja(
                    repartidorId = repartidorId,
                    queja = comentario.value.trim(),
                    addAmonestacion = rating.intValue < 3
                )
                _isLoading.value = false
                if (success) {
                    rating.intValue = 0
                    comentario.value = ""
                    _errorMessage.value = null
                    onSuccess()
                } else {
                    _errorMessage.value = "No se pudo registrar el feedback"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al enviar feedback: ${e.message}"
            }
        }
    }
}
