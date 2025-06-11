package com.example.cletaeatsapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var pedidos = mutableStateOf<List<Pedido>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var selectedCombos = mutableStateOf<List<Int>>(emptyList())

    init {
        loadPedidosWithRestaurantNames()
    }

    private fun loadPedidosWithRestaurantNames() {
        viewModelScope.launch {
            isLoading.value = true
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
                this@OrderViewModel.pedidos.value = updatedPedidos
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = "Error al cargar pedidos: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getPedidoById(pedidoId: String): Pedido? {
        return pedidos.value.find { it.id == pedidoId }
    }

    fun addCombo(combo: Int) {
        if (combo in 1..9 && !selectedCombos.value.contains(combo)) {
            val currentCombos = selectedCombos.value.toMutableList()
            currentCombos.add(combo)
            selectedCombos.value = currentCombos
        }
    }

    fun removeCombo(combo: Int) {
        val currentCombos = selectedCombos.value.toMutableList()
        currentCombos.remove(combo)
        selectedCombos.value = currentCombos
    }

    fun createOrder(clienteId: String, restauranteId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (selectedCombos.value.isEmpty()) {
                errorMessage.value = "Seleccione al menos un combo."
                return@launch
            }
            if (selectedCombos.value.any { it !in 1..9 }) {
                errorMessage.value = "Combos inválidos seleccionados."
                return@launch
            }
            isLoading.value = true
            try {
                val pedido = repository.createOrder(clienteId, restauranteId, selectedCombos.value)
                if (pedido == null) {
                    errorMessage.value = "No hay repartidores disponibles."
                } else {
                    errorMessage.value = null
                    selectedCombos.value = emptyList()
                    loadPedidosWithRestaurantNames()
                    onSuccess()
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al crear pedido: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val success = repository.updateOrderStatus(orderId, newStatus)
                if (success) {
                    errorMessage.value = null
                    loadPedidosWithRestaurantNames()
                } else {
                    errorMessage.value = "No se pudo actualizar el pedido."
                }
            } catch (e: Exception) {
                errorMessage.value = "Error al actualizar pedido: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
