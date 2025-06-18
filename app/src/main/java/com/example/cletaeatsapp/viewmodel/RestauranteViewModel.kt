package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RestauranteViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _restaurantes = MutableStateFlow<List<Restaurante>>(emptyList())
    val restaurantes = _restaurantes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _ordenes = MutableStateFlow<List<Pedido>>(emptyList())
    val ordenes = _ordenes.asStateFlow()

    private val _ingresos = MutableStateFlow(0.0)
    val ingresos = _ingresos.asStateFlow()

    fun loadRestaurantData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val allRestaurants = repository.getRestaurantes()
                _restaurantes.value = allRestaurants
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar restaurantes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            if (repository.updateOrderStatus(orderId, newStatus)) {
                val updatedOrders = _ordenes.value.map { order ->
                    if (order.id == orderId) {
                        order.copy(
                            estado = newStatus,
                            horaEntregado = if (newStatus == "entregado") {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                                    Date()
                                )
                            } else {
                                order.horaEntregado
                            }
                        )
                    } else {
                        order
                    }
                }
                _ordenes.value = updatedOrders
            }
        }
    }
}
