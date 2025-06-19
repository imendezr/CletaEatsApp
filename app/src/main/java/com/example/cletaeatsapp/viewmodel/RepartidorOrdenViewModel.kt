package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RepartidorOrdenViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPedidosWithRestaurantNames()
        Timber.d("RepartidorOrdenViewModel initialized")
    }

    fun getPedidosForRepartidor(repartidorId: String): StateFlow<List<Pedido>> {
        val filteredPedidos = MutableStateFlow<List<Pedido>>(emptyList())
        viewModelScope.launch {
            _pedidos.collect { currentPedidos ->
                try {
                    if (repartidorId.isBlank()) {
                        Timber.e("Invalid repartidorId: empty or blank")
                        _errorMessage.value = "ID de repartidor inválido"
                        filteredPedidos.value = emptyList()
                        return@collect
                    }
                    val filtered = currentPedidos.filter { it.repartidorId == repartidorId }
                    Timber.d("Filtering pedidos for repartidorId: $repartidorId, found: ${filtered.size}")
                    filteredPedidos.value = filtered
                    if (filtered.isEmpty()) {
                        Timber.w("No pedidos found for repartidorId: $repartidorId")
                        _errorMessage.value = "No hay pedidos asignados"
                    } else {
                        _errorMessage.value = null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error filtering pedidos for repartidorId: $repartidorId")
                    _errorMessage.value = "Error al filtrar pedidos: ${e.message}"
                    filteredPedidos.value = emptyList()
                }
            }
        }
        return filteredPedidos.asStateFlow()
    }

    internal fun loadPedidosWithRestaurantNames() {
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
                        nombreRestaurante = restaurantes[pedido.restauranteId]?.nombre
                            ?: pedido.restauranteId
                    )
                }
                Timber.d("Loaded ${updatedPedidos.size} pedidos")
                _pedidos.value = updatedPedidos
                _errorMessage.value = null
            } catch (e: Exception) {
                Timber.e(e, "Error loading pedidos")
                _errorMessage.value = "Error al cargar pedidos: ${e.message}"
            } finally {
                _isLoading.value = false
                Timber.d("Pedidos loading completed, isLoading: ${_isLoading.value}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, repartidorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.updateOrderStatus(
                    orderId,
                    newStatus,
                    UserType.RepartidorUser(
                        Repartidor(
                            id = repartidorId,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            0.0,
                            0.0,
                            0.0,
                            0,
                            emptyList(),
                            ""
                        )
                    ),
                    repartidorId
                )
                if (success) {
                    _errorMessage.value = null
                    loadPedidosWithRestaurantNames()
                    Timber.d("Order status updated successfully for orderId: $orderId")
                } else {
                    _errorMessage.value = "No se pudo actualizar el estado del pedido"
                    Timber.w("Failed to update order status for orderId: $orderId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating order status for orderId: $orderId")
                _errorMessage.value = "Error al actualizar pedido: ${e.message}"
            } finally {
                _isLoading.value = false
                Timber.d("Order status update completed, isLoading: ${_isLoading.value}")
            }
        }
    }
}