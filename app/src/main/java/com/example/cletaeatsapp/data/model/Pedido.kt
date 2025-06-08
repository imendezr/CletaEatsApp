package com.example.cletaeatsapp.data.model

data class Pedido(
    val id: String,
    val clienteId: String,
    val restauranteId: String,
    val combos: List<Int>, // Lista de números de combos (1 a 9)
    val precio: Double,
    val costoTransporte: Double,
    val iva: Double,
    val total: Double,
    val estado: String, // "en preparación", "en camino", "suspendido", "entregado"
    val horaRealizado: String,
    val horaEntregado: String?
)
