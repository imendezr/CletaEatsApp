package com.example.cletaeatsapp.data.model

data class Repartidor(
    val id: String,
    val cedula: String,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String,
    val estado: String, // "disponible" o "ocupado"
    val distancia: Double,
    val costoPorKm: Double, // 1000 colones/km (días hábiles), 1500 colones/km (feriados)
    val amonestaciones: Int,
    val quejas: List<String>
)
