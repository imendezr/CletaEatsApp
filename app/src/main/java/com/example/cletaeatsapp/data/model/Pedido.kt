package com.example.cletaeatsapp.data.model

data class Pedido(
    val id: String,
    val clienteId: String,
    val restauranteId: String,
    val repartidorId: String?,
    val combos: List<PedidoCombo>, // Lista de combos con detalles
    val precio: Double, // Subtotal de los combos
    val distancia: Double, // Distancia del pedido en km
    val costoTransporte: Double, // Calculado según distancia y costoPorKm
    val iva: Double, // 13% del precio
    val total: Double, // precio + costoTransporte + iva
    val estado: String, // "en preparación", "en camino", "suspendido", "entregado"
    val horaRealizado: String,
    val horaEntregado: String?,
    val nombreRestaurante: String? = null
)

data class PedidoCombo(
    val numero: Int, // 1 a 9
    val nombre: String,
    val precio: Double
)
