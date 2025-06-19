package com.example.cletaeatsapp.data.model

import com.squareup.moshi.JsonClass

sealed class UserType {
    @JsonClass(generateAdapter = true)
    data class ClienteUser(val cliente: Cliente) : UserType()

    @JsonClass(generateAdapter = true)
    data class RepartidorUser(val repartidor: Repartidor) : UserType()

    @JsonClass(generateAdapter = true)
    data class RestauranteUser(val restaurante: Restaurante) : UserType()

    @JsonClass(generateAdapter = true)
    data class AdminUser(val admin: Admin) : UserType()
}
