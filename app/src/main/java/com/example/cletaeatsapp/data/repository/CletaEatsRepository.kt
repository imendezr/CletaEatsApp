package com.example.cletaeatsapp.data.repository

import android.content.Context
import androidx.compose.ui.unit.dp
import com.example.cletaeatsapp.data.model.Admin
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.PedidoCombo
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.model.RestauranteCombo
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.network.AddQuejaRequest
import com.example.cletaeatsapp.data.network.AuthRequest
import com.example.cletaeatsapp.data.network.AuthResult
import com.example.cletaeatsapp.data.network.CletaEatsApiService
import com.example.cletaeatsapp.data.network.CreateOrderRequest
import com.example.cletaeatsapp.data.network.UpdateStatusRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

data class CletaEatsRepository @Inject constructor(
    private val context: Context,
    private val apiService: CletaEatsApiService
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    suspend fun getUserByCedula(cedula: String): UserType? = withContext(Dispatchers.IO) {
        try {
            // Suponiendo que el backend no tiene un endpoint específico para verificar cédula sin autenticar,
            // buscamos en los tipos de usuario (clientes, repartidores, restaurantes, admins)
            val clientes = apiService.getClientes()
            clientes.find { it.cedula == cedula }
                ?.let { return@withContext UserType.ClienteUser(it) }

            val repartidores = apiService.getRepartidores()
            repartidores.find { it.cedula == cedula }
                ?.let { return@withContext UserType.RepartidorUser(it) }

            val restaurantes = apiService.getRestaurantes()
            restaurantes.find { it.cedulaJuridica == cedula }
                ?.let { return@withContext UserType.RestauranteUser(it) }

            val admins = apiService.getAdmins()
            admins.find { it.nombreUsuario == cedula }
                ?.let { return@withContext UserType.AdminUser(it) }

            null
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user by cedula")
            null
        }
    }

    suspend fun saveClientes(clientes: List<Cliente>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = gson.toJson(clientes)
            context.openFileOutput("clientes.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            val clientes = apiService.getClientes()
            saveClientes(clientes) // Persistir en archivo
            clientes
        } catch (e: Exception) {
            Timber.e(e, "Error fetching clients, loading from local")
            loadLocalClientes()
        }
    }

    suspend fun registerCliente(cliente: Cliente): Boolean = withContext(Dispatchers.IO) {
        if (!isCedulaValid(cliente.cedula) || !isContrasenaValid(cliente.contrasena) || cliente.isInvalid()) return@withContext false
        try {
            val success = apiService.registerCliente(cliente)
            if (success) {
                val clientes = getClientes().toMutableList().apply { add(cliente) }
                saveClientes(clientes)
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "Error registering cliente")
            false
        }
    }

    suspend fun saveRepartidores(repartidores: List<Repartidor>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = gson.toJson(repartidores)
            context.openFileOutput("repartidores.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getRepartidores(): List<Repartidor> = withContext(Dispatchers.IO) {
        try {
            val repartidores = apiService.getRepartidores()
            saveRepartidores(repartidores)
            repartidores
        } catch (e: Exception) {
            Timber.e(e, "Error fetching repartidores, loading from local")
            loadLocalRepartidores()
        }
    }

    suspend fun registerRepartidor(repartidor: Repartidor): Boolean = withContext(Dispatchers.IO) {
        if (!isCedulaValid(repartidor.cedula) || !isContrasenaValid(repartidor.contrasena) || repartidor.isInvalid()) return@withContext false
        try {
            val success = apiService.registerRepartidor(repartidor)
            if (success) {
                val repartidores = getRepartidores().toMutableList().apply { add(repartidor) }
                saveRepartidores(repartidores)
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "Error registering repartidor")
            false
        }
    }

    suspend fun addQueja(repartidorId: String, queja: String, addAmonestacion: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val success =
                    apiService.addQueja(repartidorId, AddQuejaRequest(queja, addAmonestacion))
                if (success) {
                    val repartidores = getRepartidores()
                    saveRepartidores(repartidores) // Actualizar archivo
                }
                success
            } catch (e: Exception) {
                Timber.e(e, "Error adding queja")
                false
            }
        }

    suspend fun resetRepartidoresEstado(): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = apiService.resetRepartidoresEstado()
            if (success) {
                val repartidores = getRepartidores()
                saveRepartidores(repartidores)
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "Error resetting repartidores estado")
            false
        }
    }

    suspend fun saveRestaurantes(restaurantes: List<Restaurante>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = gson.toJson(restaurantes)
            context.openFileOutput("restaurantes.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getRestaurantes(): List<Restaurante> = withContext(Dispatchers.IO) {
        try {
            val restaurantes = apiService.getRestaurantes()
            saveRestaurantes(restaurantes)
            restaurantes
        } catch (e: Exception) {
            Timber.e(e, "Error fetching restaurantes, loading from local")
            loadLocalRestaurantes()
        }
    }

    suspend fun registerRestaurante(restaurante: Restaurante): Boolean =
        withContext(Dispatchers.IO) {
            if (!isCedulaJuridicaValid(restaurante.cedulaJuridica) || !isContrasenaValid(restaurante.contrasena) || restaurante.isInvalid()) return@withContext false
            try {
                val success = apiService.registerRestaurante(restaurante)
                if (success) {
                    val restaurantes = getRestaurantes().toMutableList().apply { add(restaurante) }
                    saveRestaurantes(restaurantes)
                }
                success
            } catch (e: Exception) {
                Timber.e(e, "Error registering restaurante")
                false
            }
        }

    suspend fun saveAdmins(admins: List<Admin>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = gson.toJson(admins)
            context.openFileOutput("admins.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getAdmins(): List<Admin> = withContext(Dispatchers.IO) {
        try {
            val admins = apiService.getAdmins()
            saveAdmins(admins)
            admins
        } catch (e: Exception) {
            Timber.e(e, "Error fetching admins, loading from local")
            loadLocalAdmins()
        }
    }

    suspend fun savePedidos(pedidos: List<Pedido>) = withContext(Dispatchers.IO) {
        retryIO(3) {
            val json = gson.toJson(pedidos)
            context.openFileOutput("pedidos.txt", Context.MODE_PRIVATE)
                .use { it.write(json.toByteArray()) }
        }
    }

    suspend fun getPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        try {
            val pedidos = apiService.getPedidos()
            savePedidos(pedidos)
            pedidos
        } catch (e: Exception) {
            Timber.e(e, "Error fetching pedidos, loading from local")
            loadLocalPedidos()
        }
    }

    suspend fun createOrder(
        clienteId: String,
        restauranteId: String,
        combos: List<PedidoCombo>,
        distancia: Double
    ): Pedido? = withContext(Dispatchers.IO) {
        try {
            val pedido = apiService.createOrder(
                CreateOrderRequest(
                    clienteId,
                    restauranteId,
                    combos,
                    distancia
                )
            )
            if (pedido != null) {
                val pedidos = getPedidos().toMutableList().apply { add(pedido) }
                savePedidos(pedidos)
            }
            pedido
        } catch (e: Exception) {
            Timber.e(e, "Error creating order")
            null
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        userType: UserType,
        userId: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = apiService.updateOrderStatus(
                orderId,
                UpdateStatusRequest(newStatus, userType.toString(), userId)
            )
            if (success) {
                val pedidos = getPedidos()
                savePedidos(pedidos)
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "Error updating order status")
            false
        }
    }

    suspend fun authenticateUser(cedula: String, contrasena: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                apiService.authenticateUser(AuthRequest(cedula, contrasena))
            } catch (e: Exception) {
                Timber.e(e, "Authentication error")
                AuthResult.Error("Error al autenticar: ${e.message}")
            }
        }

    suspend fun updateClienteProfile(cliente: Cliente): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = apiService.updateClienteProfile(cliente.cedula, cliente)
            if (success) {
                val clientes = getClientes()
                saveClientes(clientes)
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "Error updating cliente profile")
            false
        }
    }

    suspend fun updateRepartidorProfile(repartidor: Repartidor): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val success = apiService.updateRepartidorProfile(repartidor.cedula, repartidor)
                if (success) {
                    val repartidores = getRepartidores()
                    saveRepartidores(repartidores)
                }
                success
            } catch (e: Exception) {
                Timber.e(e, "Error updating repartidor profile")
                false
            }
        }

    suspend fun updateRestauranteProfile(restaurante: Restaurante): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val success =
                    apiService.updateRestauranteProfile(restaurante.cedulaJuridica, restaurante)
                if (success) {
                    val restaurantes = getRestaurantes()
                    saveRestaurantes(restaurantes)
                }
                success
            } catch (e: Exception) {
                Timber.e(e, "Error updating restaurante profile")
                false
            }
        }

    suspend fun getActiveClients(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            val clients = apiService.getActiveClients()
            saveClientes(clients)
            clients
        } catch (e: Exception) {
            Timber.e(e, "Error fetching active clients")
            loadLocalClientes().filter { it.estado == "activo" }
        }
    }

    suspend fun getSuspendedClients(): List<Cliente> = withContext(Dispatchers.IO) {
        try {
            val clients = apiService.getSuspendedClients()
            saveClientes(clients)
            clients
        } catch (e: Exception) {
            Timber.e(e, "Error fetching suspended clients")
            loadLocalClientes().filter { it.estado == "suspendido" }
        }
    }

    suspend fun getRepartidoresSinAmonestaciones(): List<Repartidor> = withContext(Dispatchers.IO) {
        try {
            val repartidores = apiService.getRepartidoresSinAmonestaciones()
            saveRepartidores(repartidores)
            repartidores
        } catch (e: Exception) {
            Timber.e(e, "Error fetching repartidores sin amonestaciones")
            loadLocalRepartidores().filter { it.amonestaciones == 0 }
        }
    }

    suspend fun getTotalRevenueByRestaurant(): Map<String, Double> = withContext(Dispatchers.IO) {
        try {
            apiService.getTotalRevenueByRestaurant()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching revenue by restaurant")
            emptyMap()
        }
    }

    suspend fun getRestauranteConMasPedidos(): Restaurante? = withContext(Dispatchers.IO) {
        try {
            apiService.getRestauranteConMasPedidos()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching restaurante con más pedidos")
            null
        }
    }

    suspend fun getRestauranteConMenosPedidos(): Restaurante? = withContext(Dispatchers.IO) {
        try {
            apiService.getRestauranteConMenosPedidos()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching restaurante con menos pedidos")
            null
        }
    }

    suspend fun getQuejasPorRepartidor(): Map<String, List<String>> = withContext(Dispatchers.IO) {
        try {
            apiService.getQuejasPorRepartidor()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching quejas por repartidor")
            emptyMap()
        }
    }

    suspend fun getPedidosPorCliente(): Map<String, List<Pedido>> = withContext(Dispatchers.IO) {
        try {
            apiService.getPedidosPorCliente()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching pedidos por cliente")
            emptyMap()
        }
    }

    suspend fun getClienteConMasPedidos(): Cliente? = withContext(Dispatchers.IO) {
        try {
            apiService.getClienteConMasPedidos()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching cliente con más pedidos")
            null
        }
    }

    suspend fun getHoraPicoPedidos(): String? = withContext(Dispatchers.IO) {
        try {
            apiService.getHoraPicoPedidos()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching hora pico")
            null
        }
    }

    private fun Cliente.isInvalid() =
        nombre.isBlank() || direccion.isBlank() || telefono.isBlank() || correo.isBlank() || numeroTarjeta.isBlank()

    private fun Repartidor.isInvalid() =
        nombre.isBlank() || direccion.isBlank() || telefono.isBlank() || correo.isBlank() || costoPorKmHabiles <= 0 || costoPorKmFeriados <= 0

    private fun Restaurante.isInvalid() =
        nombre.isBlank() || direccion.isBlank() || tipoComida.isBlank() || !areCombosValid(combos)

    private fun isCedulaValid(cedula: String) = cedula.length == 9 && cedula.all { it.isDigit() }
    private fun isCedulaJuridicaValid(cedula: String) =
        cedula.length == 10 && cedula.all { it.isDigit() }

    private fun isContrasenaValid(password: String) = password.length >= 8
    private fun areCombosValid(combos: List<RestauranteCombo>) =
        combos.all { it.numero in 1..9 && it.nombre.isNotBlank() && it.precio > 0 }

    private suspend fun loadLocalClientes(): List<Cliente> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("clientes.txt")) return@withContext emptyList()
        try {
            retryIO(3) {
                context.openFileInput("clientes.txt").use {
                    gson.fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Cliente>::class.java
                    ).toList()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading local clientes")
            emptyList()
        }
    }

    private suspend fun loadLocalRepartidores(): List<Repartidor> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("repartidores.txt")) return@withContext emptyList()
        try {
            retryIO(3) {
                context.openFileInput("repartidores.txt").use {
                    gson.fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Repartidor>::class.java
                    ).toList()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading local repartidores")
            emptyList()
        }
    }

    private suspend fun loadLocalRestaurantes(): List<Restaurante> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("restaurantes.txt")) return@withContext emptyList()
        try {
            retryIO(3) {
                context.openFileInput("restaurantes.txt").use {
                    gson.fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Restaurante>::class.java
                    ).toList()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading local restaurantes")
            emptyList()
        }
    }

    private suspend fun loadLocalAdmins(): List<Admin> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("admins.txt")) return@withContext emptyList()
        try {
            retryIO(3) {
                context.openFileInput("admins.txt").use {
                    gson.fromJson(it.readBytes().toString(Charsets.UTF_8), Array<Admin>::class.java)
                        .toList()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading local admins")
            emptyList()
        }
    }

    private suspend fun loadLocalPedidos(): List<Pedido> = withContext(Dispatchers.IO) {
        if (!context.fileList().contains("pedidos.txt")) return@withContext emptyList()
        try {
            retryIO(3) {
                context.openFileInput("pedidos.txt").use {
                    gson.fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Pedido>::class.java
                    ).toList()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading local pedidos")
            emptyList()
        }
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

object Dimens {
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
}
