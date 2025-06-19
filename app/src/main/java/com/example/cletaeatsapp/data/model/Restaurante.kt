package com.example.cletaeatsapp.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Restaurante(
    val id: String,
    val cedulaJuridica: String,
    val nombre: String,
    val direccion: String,
    val tipoComida: String,
    val contrasena: String,
    val combos: List<RestauranteCombo> // Lista de combos
)

@JsonClass(generateAdapter = true)
data class RestauranteCombo(
    val numero: Int, // 1 a 9
    val nombre: String, // Nombre del combo
    val precio: Double // Precio del combo
)
