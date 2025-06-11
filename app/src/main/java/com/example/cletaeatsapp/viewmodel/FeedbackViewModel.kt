package com.example.cletaeatsapp.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var rating = mutableIntStateOf(0)
    var comentario = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    fun submitFeedback(repartidorId: String?, onSuccess: () -> Unit) {
        if (repartidorId == null) {
            errorMessage.value = "Error: Repartidor no asignado"
            return
        }
        if (rating.intValue == 0) {
            errorMessage.value = "Por favor seleccione una calificación"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            try {
                repository.addQueja(
                    repartidorId = repartidorId,
                    queja = comentario.value,
                    addAmonestacion = rating.intValue < 3
                )
                isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                isLoading.value = false
                errorMessage.value = "Error al enviar feedback: ${e.message}"
            }
        }
    }
}
