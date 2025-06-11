package com.example.cletaeatsapp.data.network

import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.Restaurante

interface CletaEatsApiService {
    suspend fun getClientes(): List<Cliente>
    suspend fun registerCliente(cliente: Cliente): Boolean
    suspend fun getRestaurantes(): List<Restaurante>
    suspend fun getPedidos(): List<Pedido>
    suspend fun createPedido(clienteId: String, restauranteId: String, combos: List<Int>): Pedido?
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean
    suspend fun getRepartidores(): List<Repartidor>
}
