package com.example.cletaeatsapp.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cliente(
    val id: String,
    val cedula: String,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String,
    val estado: String, // "activo" o "suspendido"
    val contrasena: String,
    val numeroTarjeta: String
)
