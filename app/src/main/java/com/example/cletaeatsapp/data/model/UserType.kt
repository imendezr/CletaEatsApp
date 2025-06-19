package com.example.cletaeatsapp.data.model

sealed class UserType {
    data class ClientUser(val cliente: Cliente) : UserType()
    data class RepartidorUser(val repartidor: Repartidor) : UserType()
    data class RestauranteUser(val restaurante: Restaurante) : UserType()
    data class AdminUser(val admin: Admin) : UserType()
}
