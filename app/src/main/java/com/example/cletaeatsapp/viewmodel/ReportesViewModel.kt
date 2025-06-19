package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Repartidor
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
                val clientes = repository.getClientes()
                val repartidores = repository.getRepartidores()
                val restaurantes = repository.getRestaurantes()
                val pedidos = repository.getPedidos()
                val activeClients = repository.getActiveClients()
                val suspendedClients = repository.getSuspendedClients()
                val repartidoresSinAmonestaciones = repository.getRepartidoresSinAmonestaciones()
                val revenueByRestaurant = repository.getTotalRevenueByRestaurant()
                val restauranteConMasPedidos = repository.getRestauranteConMasPedidos()
                val restauranteConMenosPedidos = repository.getRestauranteConMenosPedidos()
                val quejasPorRepartidor = repository.getQuejasPorRepartidor()
                val pedidosPorCliente = repository.getPedidosPorCliente()
                val clienteConMasPedidos = repository.getClienteConMasPedidos()
                val horaPico = repository.getHoraPicoPedidos()
                val totalRevenue = pedidos.sumOf { it.total }

                _uiState.value = ReportsUiState.Success(
                    activeClients = activeClients,
                    suspendedClients = suspendedClients,
                    repartidoresSinAmonestaciones = repartidoresSinAmonestaciones,
                    restaurantes = restaurantes,
                    pedidos = pedidos,
                    revenueByRestaurant = revenueByRestaurant,
                    restauranteConMasPedidos = restauranteConMasPedidos,
                    restauranteConMenosPedidos = restauranteConMenosPedidos,
                    quejasPorRepartidor = quejasPorRepartidor,
                    pedidosPorCliente = pedidosPorCliente,
                    clienteConMasPedidos = clienteConMasPedidos,
                    horaPico = horaPico,
                    totalRevenue = totalRevenue
                )
            } catch (e: Exception) {
                _uiState.value = ReportsUiState.Error("Error al cargar reportes: ${e.message}")
            }
        }
    }
}

sealed class ReportsUiState {
    data object Loading : ReportsUiState()
    data class Success(
        val activeClients: List<Cliente>,
        val suspendedClients: List<Cliente>,
        val repartidoresSinAmonestaciones: List<Repartidor>,
        val restaurantes: List<Restaurante>,
        val pedidos: List<Pedido>,
        val revenueByRestaurant: Map<String, Double>,
        val restauranteConMasPedidos: Restaurante?,
        val restauranteConMenosPedidos: Restaurante?,
        val quejasPorRepartidor: Map<String, List<String>>,
        val pedidosPorCliente: Map<String, List<Pedido>>,
        val clienteConMasPedidos: Cliente?,
        val horaPico: String?,
        val totalRevenue: Double
    ) : ReportsUiState()

    data class Error(val message: String) : ReportsUiState()
}
