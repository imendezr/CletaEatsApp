package com.example.cletaeatsapp.data.repository

import android.content.Context
import android.util.Log
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.Restaurante
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
        scope.launch {
            initializeData()
        }
    }

    private suspend fun initializeData() {
        withContext(Dispatchers.IO) {
            try {
                if (!context.fileList().contains("clientes.txt")) {
                    Log.d("CletaEatsRepository", "clientes.txt not found, initializing")
                    initializeMockClientes()
                }
                if (!context.fileList().contains("restaurantes.txt")) {
                    Log.d("CletaEatsRepository", "restaurantes.txt not found, initializing")
                    initializeMockRestaurants()
                }
                if (!context.fileList().contains("pedidos.txt")) {
                    Log.d("CletaEatsRepository", "pedidos.txt not found, initializing")
                    initializeMockPedidos()
                }
                if (!context.fileList().contains("repartidores.txt")) {
                    Log.d("CletaEatsRepository", "repartidores.txt not found, initializing")
                    initializeMockRepartidores()
                }
            } catch (e: Exception) {
                Log.e("CletaEatsRepository", "Failed to initialize data", e)
            }
        }
    }

    suspend fun saveClientes(clientes: List<Cliente>) = withContext(Dispatchers.IO) {
        Log.d("CletaEatsRepository", "Saving ${clientes.size} clientes to clientes.txt")
        retryIO(3) {
            val json = Gson().toJson(clientes)
            context.openFileOutput("clientes.txt", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        }
        Log.d("CletaEatsRepository", "Clientes saved successfully")
    }

    suspend fun getClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        Log.d("CletaEatsRepository", "Fetching clientes from clientes.txt")
        try {
            retryIO(3) {
                context.openFileInput("clientes.txt").use {
                    val json = it.readBytes().toString(Charsets.UTF_8)
                    Gson().fromJson(json, Array<Cliente>::class.java).toList()
                }
            }.also {
                Log.d("CletaEatsRepository", "Fetched ${it.size} clientes")
            }
        } catch (e: Exception) {
            Log.e("CletaEatsRepository", "Failed to fetch clientes", e)
            initializeMockClientes()
            listOf(
                Cliente(
                    id = UUID.randomUUID().toString(),
                    cedula = "123456789",
                    nombre = "Ana López",
                    direccion = "San José, Costa Rica",
                    telefono = "88888888",
                    correo = "ana@example.com",
                    estado = "activo"
                ),
                Cliente(
                    id = UUID.randomUUID().toString(),
                    cedula = "987654321",
                    nombre = "Carlos Ramírez",
                    direccion = "Heredia, Costa Rica",
                    telefono = "77777777",
                    correo = "carlos@example.com",
                    estado = "activo"
                ),
                Cliente(
                    id = UUID.randomUUID().toString(),
                    cedula = "118090020",
                    nombre = "María Gómez",
                    direccion = "Alajuela, Costa Rica",
                    telefono = "66666666",
                    correo = "maria@example.com",
                    estado = "activo"
                )
            ).also {
                Log.d("CletaEatsRepository", "Returning ${it.size} mock clientes")
            }
        }
    }

    suspend fun saveRestaurantes(restaurantes: List<Restaurante>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(restaurantes)
            context.openFileOutput("restaurantes.txt", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        }
        Log.d("CletaEatsRepository", "Restaurantes saved successfully")
    }

    suspend fun getRestaurantes(): List<Restaurante> = withContext(Dispatchers.IO) {
        Log.d("CletaEatsRepository", "Fetching restaurantes from restaurantes.txt")
        try {
            retryIO(3) {
                context.openFileInput("restaurantes.txt").use {
                    val json = it.readBytes().toString(Charsets.UTF_8)
                    Gson().fromJson(json, Array<Restaurante>::class.java).toList()
                }
            }.also {
                Log.d("CletaEatsRepository", "Fetched ${it.size} restaurantes")
            }
        } catch (e: Exception) {
            Log.e("CletaEatsRepository", "Failed to fetch restaurantes", e)
            initializeMockRestaurants()
            listOf(
                Restaurante(
                    id = UUID.randomUUID().toString(),
                    cedulaJuridica = "300123456789",
                    nombre = "La Pizzeria",
                    direccion = "San José",
                    tipoComida = "Italiana"
                ),
                Restaurante(
                    id = UUID.randomUUID().toString(),
                    cedulaJuridica = "300987654321",
                    nombre = "Taco Loco",
                    direccion = "Heredia",
                    tipoComida = "Mexicana"
                )
            ).also {
                Log.d("CletaEatsRepository", "Returning ${it.size} mock restaurantes")
            }
        }
    }

    suspend fun savePedidos(pedidos: List<Pedido>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(pedidos)
            context.openFileOutput("pedidos.txt", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        }
        Log.d("CletaEatsRepository", "Pedidos saved successfully")
    }

    suspend fun getPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        Log.d("CletaEatsRepository", "Fetching pedidos from pedidos.txt")
        try {
            retryIO(3) {
                context.openFileInput("pedidos.txt").use {
                    val json = it.readBytes().toString(Charsets.UTF_8)
                    Gson().fromJson(json, Array<Pedido>::class.java).toList()
                }
            }.also {
                Log.d("CletaEatsRepository", "Fetched ${it.size} pedidos")
            }
        } catch (e: Exception) {
            Log.e("CletaEatsRepository", "Failed to fetch pedidos", e)
            initializeMockPedidos()
            listOf(
                Pedido(
                    id = UUID.randomUUID().toString(),
                    clienteId = "123456789",
                    restauranteId = UUID.randomUUID().toString(),
                    repartidorId = UUID.randomUUID().toString(),
                    combos = listOf(1, 2),
                    precio = 9000.0,
                    costoTransporte = 5000.0,
                    iva = 1170.0,
                    total = 15170.0,
                    estado = "en preparación",
                    horaRealizado = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date()),
                    horaEntregado = null
                )
            ).also {
                Log.d("CletaEatsRepository", "Returning ${it.size} mock pedidos")
            }
        }
    }

    suspend fun registerCliente(cliente: Cliente) = withContext(Dispatchers.IO) {
        Log.d("CletaEatsRepository", "Registering cliente with cedula: ${cliente.cedula}")
        val clientes = getClientes().toMutableList()
        if (clientes.none { it.cedula == cliente.cedula }) {
            clientes.add(cliente)
            saveClientes(clientes)
            Log.d("CletaEatsRepository", "Cliente registered successfully")
            true
        } else {
            Log.w("CletaEatsRepository", "Cliente already exists with cedula: ${cliente.cedula}")
            false
        }
    }

    suspend fun getRepartidores(): List<Repartidor> = withContext(Dispatchers.IO) {
        Log.d("CletaEatsRepository", "Fetching repartidores from repartidores.txt")
        try {
            retryIO(3) {
                context.openFileInput("repartidores.txt").use {
                    val json = it.readBytes().toString(Charsets.UTF_8)
                    Gson().fromJson(json, Array<Repartidor>::class.java).toList()
                }
            }.also {
                Log.d("CletaEatsRepository", "Fetched ${it.size} repartidores")
            }
        } catch (e: Exception) {
            Log.e("CletaEatsRepository", "Failed to fetch repartidores", e)
            initializeMockRepartidores()
            listOf(
                Repartidor(
                    id = UUID.randomUUID().toString(),
                    cedula = "123456789",
                    nombre = "Juan Pérez",
                    direccion = "Heredia",
                    telefono = "88888888",
                    correo = "juan@example.com",
                    estado = "disponible",
                    distancia = 5.0,
                    costoPorKm = 1000.0,
                    amonestaciones = 0,
                    quejas = emptyList()
                ),
                Repartidor(
                    id = UUID.randomUUID().toString(),
                    cedula = "987654321",
                    nombre = "María Gómez",
                    direccion = "Alajuela",
                    telefono = "77777777",
                    correo = "maria@example.com",
                    estado = "disponible",
                    distancia = 3.0,
                    costoPorKm = 1000.0,
                    amonestaciones = 1,
                    quejas = emptyList()
                )
            ).also {
                Log.d("CletaEatsRepository", "Returning ${it.size} mock repartidores")
            }
        }
    }

    suspend fun saveRepartidores(repartidores: List<Repartidor>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = Gson().toJson(repartidores)
            context.openFileOutput("repartidores.txt", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        }
        Log.d("CletaEatsRepository", "Repartidores saved successfully")
    }

    suspend fun createOrder(clienteId: String, restauranteId: String, combos: List<Int>): Pedido? =
        withContext(Dispatchers.IO) {
            val repartidores = getRepartidores()
            val repartidor = repartidores
                .filter { it.estado == "disponible" && it.amonestaciones < 4 }
                .minByOrNull { it.distancia }
            if (repartidor == null) {
                Log.w("CletaEatsRepository", "No available repartidor found")
                return@withContext null
            }

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
            Log.d("CletaEatsRepository", "Order created with id: ${pedido.id}")
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
                Log.d("CletaEatsRepository", "Order $orderId updated to status: $newStatus")
                true
            } else {
                Log.w("CletaEatsRepository", "Order $orderId not found")
                false
            }
        }

    @Suppress("unused")
    suspend fun getActiveClients(): List<Cliente> = withContext(Dispatchers.IO) {
        getClientes().filter { it.estado == "activo" }
    }

    suspend fun getTotalRevenueByRestaurant(): Map<String, Double> = withContext(Dispatchers.IO) {
        getPedidos().groupBy { it.restauranteId }
            .mapValues { entry -> entry.value.sumOf { it.total } }
    }

    private suspend fun initializeMockClientes() = withContext(Dispatchers.IO) {
        val mockClientes = listOf(
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "123456789",
                nombre = "Ana López",
                direccion = "San José, Costa Rica",
                telefono = "88888888",
                correo = "ana@example.com",
                estado = "activo"
            ),
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "987654321",
                nombre = "Carlos Ramírez",
                direccion = "Heredia, Costa Rica",
                telefono = "77777777",
                correo = "carlos@example.com",
                estado = "activo"
            ),
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "118090020",
                nombre = "María Gómez",
                direccion = "Alajuela, Costa Rica",
                telefono = "66666666",
                correo = "maria@example.com",
                estado = "activo"
            )
        )
        Log.d("CletaEatsRepository", "Initializing mock clientes")
        saveClientes(mockClientes)
        Log.d("CletaEatsRepository", "Mock clientes initialized")
    }

    private suspend fun initializeMockRestaurants() = withContext(Dispatchers.IO) {
        val mockRestaurants = listOf(
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "300123456789",
                nombre = "La Pizzeria",
                direccion = "San José",
                tipoComida = "Italiana"
            ),
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "300987654321",
                nombre = "Taco Loco",
                direccion = "Heredia",
                tipoComida = "Mexicana"
            )
        )
        Log.d("CletaEatsRepository", "Initializing mock restaurants")
        saveRestaurantes(mockRestaurants)
        Log.d("CletaEatsRepository", "Mock restaurants initialized")
    }

    private suspend fun initializeMockPedidos() = withContext(Dispatchers.IO) {
        val restaurants = getRestaurantes() // Fetch existing restaurants
        val mockPedidos = restaurants.map { restaurant ->
            Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = "123456789",
                restauranteId = restaurant.id, // Use existing restaurant ID
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
        Log.d("CletaEatsRepository", "Initializing mock pedidos")
        savePedidos(mockPedidos)
        Log.d("CletaEatsRepository", "Mock pedidos initialized")
    }

    private suspend fun initializeMockRepartidores() = withContext(Dispatchers.IO) {
        val mockRepartidores = listOf(
            Repartidor(
                id = UUID.randomUUID().toString(),
                cedula = "123456789",
                nombre = "Juan Pérez",
                direccion = "Heredia",
                telefono = "88888888",
                correo = "juan@example.com",
                estado = "disponible",
                distancia = 5.0,
                costoPorKm = 1000.0,
                amonestaciones = 0,
                quejas = emptyList()
            ),
            Repartidor(
                id = UUID.randomUUID().toString(),
                cedula = "987654321",
                nombre = "María Gómez",
                direccion = "Alajuela",
                telefono = "77777777",
                correo = "maria@example.com",
                estado = "disponible",
                distancia = 3.0,
                costoPorKm = 1000.0,
                amonestaciones = 1,
                quejas = emptyList()
            )
        )
        Log.d("CletaEatsRepository", "Initializing mock repartidores")
        saveRepartidores(mockRepartidores)
        Log.d("CletaEatsRepository", "Mock repartidores initialized")
    }

    private suspend fun <T> retryIO(
        times: Int,
        block: suspend () -> T
    ): T {
        var currentAttempt = 0
        while (currentAttempt < times) {
            try {
                Log.d("CletaEatsRepository", "RetryIO attempt ${currentAttempt + 1}/$times")
                return block()
            } catch (e: IOException) {
                currentAttempt++
                Log.w("CletaEatsRepository", "IO attempt $currentAttempt failed: ${e.message}")
                if (currentAttempt >= times) {
                    Log.e("CletaEatsRepository", "RetryIO failed after $times attempts", e)
                    throw e
                }
                delay(100L * currentAttempt)
            }
        }
        throw IOException("Failed after $times attempts")
    }
}
