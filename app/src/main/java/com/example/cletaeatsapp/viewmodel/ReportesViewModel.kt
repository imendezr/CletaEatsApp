package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            try {
                val restaurants =
                    repository.getRestaurantes().associateBy { it.id } // Map for O(1) lookup
                val revenue = repository.getTotalRevenueByRestaurant()
                val revenueData = revenue.mapNotNull { (restaurantId, total) ->
                    restaurants[restaurantId]?.let { restaurant ->
                        Pair(restaurant.nombre, total)
                    }
                }
                _uiState.value = ReportsUiState.Success(
                    revenueByRestaurant = revenueData.associate { it.first to it.second },
                    restaurants = restaurants.values.toList()
                )
            } catch (e: Exception) {
                _uiState.value = ReportsUiState.Error("Error al cargar reportes: ${e.message}")
            }
        }
    }

    fun getRestaurantName(restaurantId: String, restaurants: List<Restaurante>): String? {
        return restaurants.find { it.id == restaurantId }?.nombre
    }
}

sealed class ReportsUiState {
    data object Loading : ReportsUiState()
    data class Success(
        val revenueByRestaurant: Map<String, Double>, // Changed from Map<String, Double> with IDs to names
        val restaurants: List<Restaurante>
    ) : ReportsUiState()

    data class Error(val message: String) : ReportsUiState()
}
