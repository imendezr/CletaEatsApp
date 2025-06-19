package com.example.cletaeatsapp.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Repartidor(
    val id: String,
    val cedula: String,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String,
    val estado: String, // "disponible", "ocupado", "inactivo"
    val kmRecorridosDiarios: Double, // Kilómetros recorridos por día
    val costoPorKmHabiles: Double, // 1000 colones/km
    val costoPorKmFeriados: Double, // 1500 colones/km
    val amonestaciones: Int,
    val quejas: List<String>,
    val contrasena: String,
    val numeroTarjeta: String? = null
)
