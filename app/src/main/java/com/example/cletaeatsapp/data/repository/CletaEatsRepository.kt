package com.example.cletaeatsapp.data.repository

import android.content.Context
import com.example.cletaeatsapp.data.model.Admin
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.model.UserType
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CletaEatsRepository(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch { initializeData() }
    }

    private suspend fun initializeData() = withContext(Dispatchers.IO) {
        try {
            if (!context.fileList().contains("clientes.txt")) initializeMockClientes()
            if (!context.fileList().contains("restaurantes.txt")) initializeMockRestaurants()
            if (!context.fileList().contains("pedidos.txt")) initializeMockPedidos()
            if (!context.fileList().contains("repartidores.txt")) initializeMockRepartidores()
            if (!context.fileList().contains("admins.txt")) initializeMockAdmins()
        } catch (_: Exception) {
            // Handle initialization error silently
        }
    }

    // --- Cliente Methods ---
    suspend fun saveClientes(clientes: List<Cliente>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(clientes)
            context.openFileOutput("clientes.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("clientes.txt")) initializeMockClientes()
        try {
            retryIO(3) {
                context.openFileInput("clientes.txt").use {
                    Gson().fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Cliente>::class.java
                    ).toList()
                }
            }
        } catch (_: Exception) {
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

    // --- Repartidor Methods ---
    suspend fun saveRepartidores(repartidores: List<Repartidor>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(repartidores)
            context.openFileOutput("repartidores.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getRepartidores(): List<Repartidor> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("repartidores.txt")) initializeMockRepartidores()
        try {
            retryIO(3) {
                context.openFileInput("repartidores.txt").use {
                    Gson().fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Repartidor>::class.java
                    ).toList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun registerRepartidor(repartidor: Repartidor) = withContext(Dispatchers.IO) {
        val repartidores = getRepartidores().toMutableList()
        if (repartidores.none { it.cedula == repartidor.cedula }) {
            repartidores.add(repartidor)
            saveRepartidores(repartidores)
            true
        } else {
            false
        }
    }

    suspend fun addQueja(repartidorId: String, queja: String, addAmonestacion: Boolean) =
        withContext(Dispatchers.IO) {
            val repartidores = getRepartidores().toMutableList()
            val repartidor = repartidores.find { it.id == repartidorId } ?: return@withContext false
            val updatedQuejas =
                if (queja.isNotBlank()) repartidor.quejas + queja else repartidor.quejas
            val updatedAmonestaciones = repartidor.amonestaciones + if (addAmonestacion) 1 else 0
            val updatedRepartidor = repartidor.copy(
                quejas = updatedQuejas,
                amonestaciones = updatedAmonestaciones,
                estado = if (updatedAmonestaciones >= 4) "inactivo" else repartidor.estado
            )
            val index = repartidores.indexOf(repartidor)
            if (index >= 0) {
                repartidores[index] = updatedRepartidor
                saveRepartidores(repartidores)
                true
            } else {
                false
            }
        }

    // --- Restaurante Methods ---
    suspend fun saveRestaurantes(restaurantes: List<Restaurante>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(restaurantes)
            context.openFileOutput("restaurantes.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getRestaurantes(): List<Restaurante> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("restaurantes.txt")) initializeMockRestaurants()
        try {
            retryIO(3) {
                context.openFileInput("restaurantes.txt").use {
                    Gson().fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Restaurante>::class.java
                    ).toList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- Admin Methods ---
    suspend fun saveAdmins(admins: List<Admin>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(admins)
            context.openFileOutput("admins.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getAdmins(): List<Admin> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("admins.txt")) initializeMockAdmins()
        try {
            retryIO(3) {
                context.openFileInput("admins.txt").use {
                    Gson().fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Admin>::class.java
                    ).toList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- Pedido Methods ---
    suspend fun savePedidos(pedidos: List<Pedido>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(pedidos)
            context.openFileOutput("pedidos.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("pedidos.txt")) initializeMockPedidos()
        try {
            retryIO(3) {
                context.openFileInput("pedidos.txt").use {
                    Gson().fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Pedido>::class.java
                    ).toList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun createOrder(clienteId: String, restauranteId: String, combos: List<Int>): Pedido? =
        withContext(Dispatchers.IO) {
            val repartidores = getRepartidores()
            val repartidor = repartidores
                .filter { it.estado == "disponible" && it.amonestaciones < 4 }
                .minByOrNull { it.distancia } ?: return@withContext null

            val comboPrices = mapOf(
                1 to 4000.0, 2 to 5000.0, 3 to 6000.0, 4 to 7000.0, 5 to 8000.0,
                6 to 9000.0, 7 to 10000.0, 8 to 11000.0, 9 to 12000.0
            )
            val precio = combos.sumOf { comboPrices.getOrDefault(it, 0.0) }
            val costoTransporte = repartidor.distancia * repartidor.costoPorKm
            val iva = precio * 0.13
            val total = precio + costoTransporte + iva

            val pedido = Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = clienteId,
                restauranteId = restauranteId,
                repartidorId = repartidor.id,
                combos = combos,
                precio = precio,
                costoTransporte = costoTransporte,
                iva = iva,
                total = total,
                estado = "en preparación",
                horaRealizado = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                ),
                horaEntregado = null
            )

            val pedidos = getPedidos().toMutableList()
            pedidos.add(pedido)
            savePedidos(pedidos)
            pedido
        }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean =
        withContext(Dispatchers.IO) {
            val pedidos = getPedidos().toMutableList()
            val pedido = pedidos.find { it.id == orderId } ?: return@withContext false
            val updatedPedido = when (newStatus) {
                "entregado" -> pedido.copy(
                    estado = newStatus,
                    horaEntregado = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                )

                else -> pedido.copy(estado = newStatus)
            }
            val index = pedidos.indexOf(pedido)
            if (index >= 0) {
                pedidos[index] = updatedPedido
                savePedidos(pedidos)
                true
            } else {
                false
            }
        }

    // --- Authentication Methods ---
    suspend fun authenticateUser(cedula: String, contrasena: String): UserType? =
        withContext(Dispatchers.IO) {
            val cliente = getClientes().find { it.cedula == cedula && it.contrasena == contrasena }
            if (cliente != null) return@withContext UserType.ClientUser(cliente)

            val repartidor =
                getRepartidores().find { it.cedula == cedula && it.contrasena == contrasena }
            if (repartidor != null) return@withContext UserType.RepartidorUser(repartidor)

            val restaurante =
                getRestaurantes().find { it.cedulaJuridica == cedula && it.contrasena == contrasena }
            if (restaurante != null) return@withContext UserType.RestauranteUser(restaurante)

            val admin = getAdmins().find { it.username == cedula && it.password == contrasena }
            if (admin != null) return@withContext UserType.AdminUser(admin)

            null
        }

    // --- Profile Update Methods ---
    suspend fun updateClienteProfile(cliente: Cliente) = withContext(Dispatchers.IO) {
        val clientes = getClientes().toMutableList()
        val index = clientes.indexOfFirst { it.cedula == cliente.cedula }
        if (index >= 0) {
            clientes[index] = cliente
            saveClientes(clientes)
            true
        } else {
            false
        }
    }

    suspend fun updateRepartidorProfile(repartidor: Repartidor) = withContext(Dispatchers.IO) {
        val repartidores = getRepartidores().toMutableList()
        val index = repartidores.indexOfFirst { it.cedula == repartidor.cedula }
        if (index >= 0) {
            repartidores[index] = repartidor
            saveRepartidores(repartidores)
            true
        } else {
            false
        }
    }

    suspend fun updateRestauranteProfile(restaurante: Restaurante) = withContext(Dispatchers.IO) {
        val restaurantes = getRestaurantes().toMutableList()
        val index = restaurantes.indexOfFirst { it.cedulaJuridica == restaurante.cedulaJuridica }
        if (index >= 0) {
            restaurantes[index] = restaurante
            saveRestaurantes(restaurantes)
            true
        } else {
            false
        }
    }

    suspend fun getActiveClients(): List<Cliente> = withContext(Dispatchers.IO) {
        getClientes().filter { it.estado == "activo" }
    }

    suspend fun getTotalRevenueByRestaurant(): Map<String, Double> = withContext(Dispatchers.IO) {
        getPedidos().groupBy { it.restauranteId }.mapValues { it.value.sumOf { it.total } }
    }

    // --- Mock Data Initialization ---
    private suspend fun initializeMockClientes() = withContext(Dispatchers.IO) {
        val mockClientes = listOf(
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "123456789",
                nombre = "Ana López",
                direccion = "San José, Costa Rica",
                telefono = "88888888",
                correo = "ana@example.com",
                estado = "activo",
                contrasena = "password"
            ),
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "987654321",
                nombre = "Carlos Ramírez",
                direccion = "Heredia, Costa Rica",
                telefono = "77777777",
                correo = "carlos@example.com",
                estado = "activo",
                contrasena = "password"
            )
        )
        saveClientes(mockClientes)
    }

    private suspend fun initializeMockRestaurants() = withContext(Dispatchers.IO) {
        val mockRestaurants = listOf(
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "300123456789",
                nombre = "La Pizzeria",
                direccion = "San José",
                tipoComida = "Italiana",
                contrasena = "password"
            ),
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "300987654321",
                nombre = "Taco Loco",
                direccion = "Heredia",
                tipoComida = "Mexicana",
                contrasena = "password"
            )
        )
        saveRestaurantes(mockRestaurants)
    }

    private suspend fun initializeMockPedidos() = withContext(Dispatchers.IO) {
        val restaurants = getRestaurantes()
        val mockPedidos = restaurants.map { restaurant ->
            Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = "123456789",
                restauranteId = restaurant.id,
                repartidorId = UUID.randomUUID().toString(),
                combos = listOf(1, 2),
                precio = 9000.0,
                costoTransporte = 5000.0,
                iva = 1170.0,
                total = 15170.0,
                estado = "en preparación",
                horaRealizado = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                ),
                horaEntregado = null
            )
        }
        savePedidos(mockPedidos)
    }

    private suspend fun initializeMockRepartidores() = withContext(Dispatchers.IO) {
        val mockRepartidores = listOf(
            Repartidor(
                id = UUID.randomUUID().toString(),
                cedula = "111222333",
                nombre = "Juan Pérez",
                direccion = "Heredia",
                telefono = "88888888",
                correo = "juan@example.com",
                estado = "disponible",
                distancia = 5.0,
                costoPorKm = 1000.0,
                amonestaciones = 0,
                quejas = emptyList(),
                contrasena = "password"
            ),
            Repartidor(
                id = UUID.randomUUID().toString(),
                cedula = "444555666",
                nombre = "María Gómez",
                direccion = "Alajuela",
                telefono = "77777777",
                correo = "maria@example.com",
                estado = "disponible",
                distancia = 3.0,
                costoPorKm = 1000.0,
                amonestaciones = 1,
                quejas = emptyList(),
                contrasena = "password"
            )
        )
        saveRepartidores(mockRepartidores)
    }

    private suspend fun initializeMockAdmins() = withContext(Dispatchers.IO) {
        val mockAdmins = listOf(
            Admin(
                id = UUID.randomUUID().toString(),
                username = "admin",
                password = "admin123"
            )
        )
        saveAdmins(mockAdmins)
    }

    private suspend fun <T> retryIO(times: Int, block: suspend () -> T): T {
        var currentAttempt = 0
        while (currentAttempt < times) {
            try {
                return block()
            } catch (e: IOException) {
                currentAttempt++
                if (currentAttempt >= times) throw e
                delay(100L * currentAttempt)
            }
        }
        throw IOException("Failed after $times attempts")
    }
}
