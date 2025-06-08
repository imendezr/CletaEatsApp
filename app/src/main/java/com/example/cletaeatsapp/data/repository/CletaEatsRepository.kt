package com.example.cletaeatsapp.data.repository

import android.content.Context
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Restaurante
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CletaEatsRepository(private val context: Context) {
    suspend fun saveClientes(clientes: List<Cliente>) = withContext(Dispatchers.IO) {
        val json = Gson().toJson(clientes)
        context.openFileOutput("clientes.txt", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    suspend fun getClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            context.openFileInput("clientes.txt").use {
                val json = it.readBytes().toString(Charsets.UTF_8)
                Gson().fromJson(json, Array<Cliente>::class.java).toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRestaurantes(restaurantes: List<Restaurante>) = withContext(Dispatchers.IO) {
        val json = Gson().toJson(restaurantes)
        context.openFileOutput("restaurantes.txt", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    suspend fun getRestaurantes(): List<Restaurante> = withContext(Dispatchers.IO) {
        try {
            context.openFileInput("restaurantes.txt").use {
                val json = it.readBytes().toString(Charsets.UTF_8)
                Gson().fromJson(json, Array<Restaurante>::class.java).toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun registerCliente(cliente: Cliente) = withContext(Dispatchers.IO) {
        val clientes = getClientes().toMutableList()
        if (clientes.none { it.cedula == cliente.cedula }) {
            clientes.add(cliente)
            saveClientes(clientes)
            true
        } else {
            false
        }
    }

    suspend fun savePedidos(pedidos: List<Pedido>) = withContext(Dispatchers.IO) {
        val json = Gson().toJson(pedidos)
        context.openFileOutput("pedidos.txt", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    suspend fun getPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        try {
            context.openFileInput("pedidos.txt").use {
                val json = it.readBytes().toString(Charsets.UTF_8)
                Gson().fromJson(json, Array<Pedido>::class.java).toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
