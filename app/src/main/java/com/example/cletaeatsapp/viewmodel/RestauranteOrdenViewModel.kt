package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestauranteOrdenViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _filteredPedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val filteredPedidos: StateFlow<List<Pedido>> = _filteredPedidos.asStateFlow()

    init {
        loadPedidosWithRestaurantNames()
    }

    fun getPedidosForRestaurante(restauranteId: String): StateFlow<List<Pedido>> {
        viewModelScope.launch {
            _pedidos.map { currentPedidos ->
                currentPedidos.filter { it.restauranteId == restauranteId }
            }.collect { filteredList ->
                _filteredPedidos.value = filteredList
            }
        }
        return filteredPedidos
    }

    private fun loadPedidosWithRestaurantNames() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pedidosDeferred = async { repository.getPedidos() }
                val restaurantesDeferred = async { repository.getRestaurantes().associateBy { it.id } }
                val pedidos = pedidosDeferred.await()
                val restaurantes = restaurantesDeferred.await()
                val updatedPedidos = pedidos.map { pedido ->
                    pedido.copy(
                        restaurantName = restaurantes[pedido.restauranteId]?.nombre
                            ?: pedido.restauranteId
                    )
                }
                _pedidos.value = updatedPedidos
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar pedidos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateOrderStatus(orderId, newStatus)
                if (success) {
                    _errorMessage.value = null
                    loadPedidosWithRestaurantNames()
                } else {
                    _errorMessage.value = "No se pudo actualizar el pedido."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar pedido: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
