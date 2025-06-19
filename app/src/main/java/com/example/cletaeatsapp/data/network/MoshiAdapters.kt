package com.example.cletaeatsapp.data.network

import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.model.UserType.AdminUser
import com.example.cletaeatsapp.data.model.UserType.ClienteUser
import com.example.cletaeatsapp.data.model.UserType.RepartidorUser
import com.example.cletaeatsapp.data.model.UserType.RestauranteUser
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiAdapters {
    fun createMoshi(): Moshi {
        val userTypeAdapter = PolymorphicJsonAdapterFactory.of(UserType::class.java, "type")
            .withSubtype(ClienteUser::class.java, "cliente")
            .withSubtype(RepartidorUser::class.java, "repartidor")
            .withSubtype(RestauranteUser::class.java, "restaurante")
            .withSubtype(AdminUser::class.java, "admin")

        val authResultAdapter = PolymorphicJsonAdapterFactory.of(AuthResult::class.java, "type")
            .withSubtype(AuthResult.Success::class.java, "success")
            .withSubtype(AuthResult.Error::class.java, "error")

        return Moshi.Builder()
            .add(userTypeAdapter)
            .add(authResultAdapter)
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }
}
