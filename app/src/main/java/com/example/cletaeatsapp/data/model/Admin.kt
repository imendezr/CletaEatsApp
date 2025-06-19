package com.example.cletaeatsapp.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Admin(
    val id: String,
    val nombreUsuario: String,
    val contrasena: String
)

