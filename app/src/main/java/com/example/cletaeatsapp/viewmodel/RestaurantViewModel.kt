package com.example.cletaeatsapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var restaurantes = mutableStateOf<List<Restaurante>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    init {
        loadRestaurantes()
    }

    private fun loadRestaurantes() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                restaurantes.value = repository.getRestaurantes()
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar restaurantes: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
