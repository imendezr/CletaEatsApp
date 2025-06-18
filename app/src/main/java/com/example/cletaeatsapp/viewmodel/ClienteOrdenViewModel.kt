package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteOrdenViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos = _pedidos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _selectedCombos = MutableStateFlow<List<Int>>(emptyList())
    val selectedCombos = _selectedCombos.asStateFlow()

    init {
        loadPedidosWithRestaurantNames()
    }

    private fun loadPedidosWithRestaurantNames() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pedidosDeferred = async { repository.getPedidos() }
                val restaurantesDeferred =
                    async { repository.getRestaurantes().associateBy { it.id } }
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

    fun getPedidoById(pedidoId: String): Pedido? {
        return _pedidos.value.find { it.id == pedidoId }
    }

    fun addCombo(combo: Int) {
        if (combo in 1..9 && !_selectedCombos.value.contains(combo)) {
            val currentCombos = _selectedCombos.value.toMutableList()
            currentCombos.add(combo)
            _selectedCombos.value = currentCombos
        }
    }

    fun removeCombo(combo: Int) {
        val currentCombos = _selectedCombos.value.toMutableList()
        currentCombos.remove(combo)
        _selectedCombos.value = currentCombos
    }

    fun createOrder(clienteId: String, restauranteId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_selectedCombos.value.isEmpty()) {
                _errorMessage.value = "Seleccione al menos un combo."
                return@launch
            }
            if (_selectedCombos.value.any { it !in 1..9 }) {
                _errorMessage.value = "Combos inválidos seleccionados."
                return@launch
            }
            _isLoading.value = true
            try {
                val pedido = repository.createOrder(clienteId, restauranteId, _selectedCombos.value)
                if (pedido == null) {
                    _errorMessage.value = "No hay repartidores disponibles."
                } else {
                    _errorMessage.value = null
                    _selectedCombos.value = emptyList()
                    loadPedidosWithRestaurantNames()
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al crear pedido: ${e.message}"
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
