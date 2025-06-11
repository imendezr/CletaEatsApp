package com.example.cletaeatsapp.viewmodel

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var restaurants = mutableStateOf<List<Restaurante>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                restaurants.value = repository.getRestaurantes()
                errorMessage.value = null
                Log.d("RestaurantViewModel", "Loaded ${restaurants.value.size} restaurants")
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar restaurantes: ${e.message}"
                Log.e("RestaurantViewModel", "Failed to load restaurants", e)
            } finally {
                isLoading.value = false
            }
        }
    }
}
