package com.example.cletaeatsapp.data.repository

import android.content.Context
import com.example.cletaeatsapp.data.model.Admin
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.PedidoCombo
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.model.RestauranteCombo
import com.example.cletaeatsapp.data.model.UserType
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
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
            Timber.d("Data initialization completed")
        }
    }

    private suspend fun initializeData() = withContext(Dispatchers.IO) {
        try {
            if (!context.fileList().contains("clientes.txt")) initializeMockClientes()
            if (!context.fileList().contains("restaurantes.txt")) initializeMockRestaurants()
            if (!context.fileList().contains("repartidores.txt")) initializeMockRepartidores()
            if (!context.fileList().contains("pedidos.txt")) initializeMockPedidos()
            if (!context.fileList().contains("admins.txt")) initializeMockAdmins()
            resetRepartidoresEstado()
        } catch (e: Exception) {
            Timber.e(e, "Error initializing data")
        }
    }

    suspend fun getUserByCedula(cedula: String): UserType? = withContext(Dispatchers.IO) {
        val cliente = getClientes().find { it.cedula == cedula }
        if (cliente != null) return@withContext UserType.ClienteUser(cliente)

        val repartidor = getRepartidores().find { it.cedula == cedula }
        if (repartidor != null) return@withContext UserType.RepartidorUser(repartidor)

        val restaurante = getRestaurantes().find { it.cedulaJuridica == cedula }
        if (restaurante != null) return@withContext UserType.RestauranteUser(restaurante)

        val admin = getAdmins().find { it.nombreUsuario == cedula }
        if (admin != null) return@withContext UserType.AdminUser(admin)

        null
    }

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
                    val clientes = Gson().fromJson(
                        it.readBytes().toString(Charsets.UTF_8),
                        Array<Cliente>::class.java
                    ).toList()
                    Timber.d("Loaded clients: ${clientes.map { it.cedula to it.contrasena }}")
                    clientes
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun registerCliente(cliente: Cliente) = withContext(Dispatchers.IO) {
        if (!isCedulaValid(cliente.cedula)) return@withContext false
        if (!isContrasenaValid(cliente.contrasena)) return@withContext false
        if (cliente.nombre.isBlank() || cliente.direccion.isBlank() || cliente.telefono.isBlank() || cliente.correo.isBlank() || cliente.numeroTarjeta.isBlank()) {
            return@withContext false
        }
        val clientes = getClientes().toMutableList()
        if (clientes.none { it.cedula == cliente.cedula }) {
            clientes.add(cliente)
            saveClientes(clientes)
            true
        } else {
            false
        }
    }

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
        if (!isCedulaValid(repartidor.cedula)) return@withContext false
        if (!isContrasenaValid(repartidor.contrasena)) return@withContext false
        if (repartidor.nombre.isBlank() || repartidor.direccion.isBlank() || repartidor.telefono.isBlank() || repartidor.correo.isBlank()) {
            return@withContext false
        }
        if (repartidor.costoPorKmHabiles <= 0 || repartidor.costoPorKmFeriados <= 0) {
            return@withContext false
        }
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

    suspend fun resetRepartidoresEstado() = withContext(Dispatchers.IO) {
        val repartidores = getRepartidores().map {
            if (it.amonestaciones < 4) it.copy(estado = "disponible") else it
        }
        saveRepartidores(repartidores)
    }

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

    suspend fun registerRestaurante(restaurante: Restaurante) = withContext(Dispatchers.IO) {
        if (!isCedulaJuridicaValid(restaurante.cedulaJuridica)) return@withContext false
        if (!isContrasenaValid(restaurante.contrasena)) return@withContext false
        if (restaurante.nombre.isBlank() || restaurante.direccion.isBlank() || restaurante.tipoComida.isBlank()) {
            return@withContext false
        }
        if (!areCombosValid(restaurante.combos)) return@withContext false
        val restaurantes = getRestaurantes().toMutableList()
        if (restaurantes.none { it.cedulaJuridica == restaurante.cedulaJuridica }) {
            restaurantes.add(restaurante)
            saveRestaurantes(restaurantes)
            true
        } else {
            false
        }
    }

    private fun isCedulaValid(cedula: String) = cedula.length == 9 && cedula.all { it.isDigit() }
    private fun isCedulaJuridicaValid(cedula: String) =
        cedula.length == 10 && cedula.all { it.isDigit() }

    private fun isContrasenaValid(password: String) = password.length >= 8
    private fun areCombosValid(combos: List<RestauranteCombo>): Boolean {
        return combos.all { combo ->
            combo.numero in 1..9 &&
                    combo.nombre.isNotBlank() &&
                    combo.precio > 0
        }
    }

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

    suspend fun createOrder(
        clienteId: String,
        restauranteId: String,
        combos: List<PedidoCombo>,
        distancia: Double
    ): Pedido? = withContext(Dispatchers.IO) {
        Timber.d("Attempting to create order with clienteId: $clienteId, restauranteId: $restauranteId")
        try {
            val cliente = getClientes().find { it.id == clienteId && it.estado == "activo" }
                ?: run {
                    Timber.e("Cliente not found or not active for clienteId: $clienteId")
                    return@withContext null
                }

            val restaurante = getRestaurantes().find { it.id == restauranteId }
                ?: return@withContext null

            combos.forEach { pedidoCombo ->
                val comboValido = restaurante.combos.find {
                    it.numero == pedidoCombo.numero &&
                            it.nombre == pedidoCombo.nombre &&
                            it.precio == pedidoCombo.precio
                }
                if (comboValido == null) {
                    throw IllegalArgumentException("Combo ${pedidoCombo.numero} no válido para el restaurante ${restaurante.nombre}")
                }
            }

            val availableRepartidores = getRepartidores()
                .filter { it.estado == "disponible" && it.amonestaciones < 4 }
            Timber.d("Available repartidores: ${availableRepartidores.map { it.nombre }}")

            val repartidor = availableRepartidores.shuffled().firstOrNull()
                ?: run {
                    Timber.e("No repartidores disponibles")
                    return@withContext null
                }

            val isFeriado = false
            val costoPorKm =
                if (isFeriado) repartidor.costoPorKmFeriados else repartidor.costoPorKmHabiles
            val precio = combos.sumOf { it.precio }
            val costoTransporte = distancia * costoPorKm
            val iva = precio * 0.13
            val total = precio + costoTransporte + iva

            val pedido = Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = clienteId,
                restauranteId = restauranteId,
                repartidorId = repartidor.id,
                combos = combos,
                precio = precio,
                distancia = distancia,
                costoTransporte = costoTransporte,
                iva = iva,
                total = total,
                estado = "en preparación",
                horaRealizado = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                ),
                horaEntregado = null,
                nombreRestaurante = restaurante.nombre
            )

            val repartidores = getRepartidores().toMutableList()
            val index = repartidores.indexOfFirst { it.id == repartidor.id }
            if (index >= 0) {
                repartidores[index] = repartidor.copy(estado = "ocupado")
                saveRepartidores(repartidores)
            }

            val pedidos = getPedidos().toMutableList()
            pedidos.add(pedido)
            savePedidos(pedidos)
            Timber.d("Pedido creado: ${pedido.id}")
            pedido
        } catch (e: Exception) {
            Timber.e(e, "Error al crear pedido")
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
            if (newStatus !in listOf("en preparación", "en camino", "entregado", "suspendido")) {
                Timber.e("Invalid status: $newStatus")
                return@withContext false
            }

            val pedidos = getPedidos().toMutableList()
            val pedido = pedidos.find { it.id == orderId } ?: run {
                Timber.e("Order not found: $orderId")
                return@withContext false
            }

            // Validate role-based permissions
            when (userType) {
                is UserType.RestauranteUser -> {
                    if (userId != pedido.restauranteId) {
                        Timber.e("Restaurante $userId not authorized for order $orderId")
                        return@withContext false
                    }
                    when (pedido.estado) {
                        "en preparación" -> {
                            if (newStatus !in listOf("en camino", "suspendido")) {
                                Timber.e("Invalid transition from en preparación to $newStatus")
                                return@withContext false
                            }
                        }

                        "suspendido" -> {
                            if (newStatus !in listOf("en preparación", "en camino")) {
                                Timber.e("Invalid transition from suspendido to $newStatus")
                                return@withContext false
                            }
                        }

                        else -> {
                            Timber.e("Restaurante cannot transition from ${pedido.estado}")
                            return@withContext false
                        }
                    }
                }

                is UserType.RepartidorUser -> {
                    if (userId != pedido.repartidorId) {
                        Timber.e("Repartidor $userId not authorized for order $orderId")
                        return@withContext false
                    }
                    if (pedido.estado != "en camino" || newStatus != "entregado") {
                        Timber.e("Invalid transition from ${pedido.estado} to $newStatus by Repartidor")
                        return@withContext false
                    }
                }

                else -> {
                    Timber.e("User type ${userType::class.simpleName} not authorized to update order status")
                    return@withContext false
                }
            }

            val updatedPedido = when (newStatus) {
                "entregado" -> {
                    val repartidores = getRepartidores().toMutableList()
                    val repartidor = repartidores.find { it.id == pedido.repartidorId }
                        ?: return@withContext false
                    val updatedRepartidor = repartidor.copy(
                        kmRecorridosDiarios = repartidor.kmRecorridosDiarios + pedido.distancia,
                        estado = "disponible"
                    )
                    val repartidorIndex = repartidores.indexOfFirst { it.id == repartidor.id }
                    if (repartidorIndex >= 0) {
                        repartidores[repartidorIndex] = updatedRepartidor
                        saveRepartidores(repartidores)
                    }
                    pedido.copy(
                        estado = newStatus,
                        horaEntregado = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())
                    )
                }

                else -> pedido.copy(estado = newStatus)
            }

            val index = pedidos.indexOf(pedido)
            if (index >= 0) {
                pedidos[index] = updatedPedido
                savePedidos(pedidos)
                Timber.d("Order $orderId updated to $newStatus by ${userType::class.simpleName}")
                true
            } else {
                Timber.e("Order $orderId not found in list")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating order $orderId to $newStatus")
            false
        }
    }

    suspend fun authenticateUser(cedula: String, contrasena: String): AuthResult =
        withContext(Dispatchers.IO) {
            try {
                val cliente =
                    getClientes().find { it.cedula == cedula && it.contrasena == contrasena }
                Timber.d("Client check: cedula=$cedula, contrasena=$contrasena, found=${cliente?.cedula}, passMatch=${cliente?.contrasena == contrasena}")
                if (cliente != null) {
                    if (cliente.estado == "activo") {
                        return@withContext AuthResult.Success(UserType.ClienteUser(cliente))
                    } else {
                        return@withContext AuthResult.Error("Cliente suspendido")
                    }
                }

                val repartidor =
                    getRepartidores().find { it.cedula == cedula && it.contrasena == contrasena }
                if (repartidor != null) {
                    if (repartidor.amonestaciones >= 4) {
                        return@withContext AuthResult.Error("Repartidor con demasiadas amonestaciones")
                    }
                    if (repartidor.estado == "inactivo") {
                        return@withContext AuthResult.Error("Repartidor inactivo")
                    }
                    return@withContext AuthResult.Success(UserType.RepartidorUser(repartidor))
                }

                val restaurante =
                    getRestaurantes().find { it.cedulaJuridica == cedula && it.contrasena == contrasena }
                if (restaurante != null) {
                    return@withContext AuthResult.Success(UserType.RestauranteUser(restaurante))
                }

                val admin =
                    getAdmins().find { it.nombreUsuario == cedula && it.contrasena == contrasena }
                if (admin != null) {
                    return@withContext AuthResult.Success(UserType.AdminUser(admin))
                }

                AuthResult.Error("Cédula o contraseña incorrecta")
            } catch (e: Exception) {
                AuthResult.Error("Error al autenticar: ${e.message}")
            }
        }

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

    suspend fun getSuspendedClients(): List<Cliente> = withContext(Dispatchers.IO) {
        getClientes().filter { it.estado == "suspendido" }
    }

    suspend fun getRepartidoresSinAmonestaciones(): List<Repartidor> = withContext(Dispatchers.IO) {
        getRepartidores().filter { it.amonestaciones == 0 }
    }

    suspend fun getTotalRevenueByRestaurant(): Map<String, Double> = withContext(Dispatchers.IO) {
        getPedidos().groupBy { it.restauranteId }.mapValues { it.value.sumOf { it.total } }
    }

    suspend fun getRestauranteConMasPedidos(): Restaurante? = withContext(Dispatchers.IO) {
        val pedidosPorRestaurante = getPedidos().groupBy { it.restauranteId }
        val maxPedidos = pedidosPorRestaurante.maxByOrNull { it.value.size }
        getRestaurantes().find { it.id == maxPedidos?.key }
    }

    suspend fun getRestauranteConMenosPedidos(): Restaurante? = withContext(Dispatchers.IO) {
        val pedidosPorRestaurante = getPedidos().groupBy { it.restauranteId }
        val minPedidos = pedidosPorRestaurante.minByOrNull { it.value.size }
        getRestaurantes().find { it.id == minPedidos?.key }
    }

    suspend fun getQuejasPorRepartidor(): Map<String, List<String>> = withContext(Dispatchers.IO) {
        getRepartidores().associate { it.id to it.quejas }
    }

    suspend fun getPedidosPorCliente(): Map<String, List<Pedido>> = withContext(Dispatchers.IO) {
        getPedidos().groupBy { it.clienteId }
    }

    suspend fun getClienteConMasPedidos(): Cliente? = withContext(Dispatchers.IO) {
        val pedidosPorCliente = getPedidos().groupBy { it.clienteId }
        val maxPedidos = pedidosPorCliente.maxByOrNull { it.value.size }
        getClientes().find { it.id == maxPedidos?.key }
    }

    suspend fun getHoraPicoPedidos(): String? = withContext(Dispatchers.IO) {
        val pedidos = getPedidos()
        if (pedidos.isEmpty()) return@withContext null
        val horas = pedidos.groupBy {
            it.horaRealizado.substring(11, 13)
        }
        horas.maxByOrNull { it.value.size }?.key
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
                estado = "activo",
                contrasena = "password1234",
                numeroTarjeta = "1234567812345678"
            ),
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "987654321",
                nombre = "Carlos Ramírez",
                direccion = "Heredia, Costa Rica",
                telefono = "77777777",
                correo = "carlos@example.com",
                estado = "suspendido",
                contrasena = "password1234",
                numeroTarjeta = "8765432187654321"
            ),
            Cliente(
                id = UUID.randomUUID().toString(),
                cedula = "555666777",
                nombre = "Laura Fernández",
                direccion = "Alajuela, Costa Rica",
                telefono = "66666666",
                correo = "laura@example.com",
                estado = "activo",
                contrasena = "password1234",
                numeroTarjeta = "1111222233334444"
            )
        )
        saveClientes(mockClientes)
    }

    private suspend fun initializeMockRestaurants() = withContext(Dispatchers.IO) {
        val mockRestaurants = listOf(
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "3001234567",
                nombre = "La Pizzeria",
                direccion = "San José, Costa Rica",
                tipoComida = "Italiana",
                contrasena = "password1234",
                combos = listOf(
                    RestauranteCombo(1, "Pizza Margherita", 4000.0),
                    RestauranteCombo(2, "Pizza Pepperoni", 5000.0),
                    RestauranteCombo(3, "Pizza Suprema", 6000.0)
                )
            ),
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "3009876543",
                nombre = "Taco Loco",
                direccion = "Heredia, Costa Rica",
                tipoComida = "Mexicana",
                contrasena = "password1234",
                combos = listOf(
                    RestauranteCombo(1, "Taco al Pastor", 4000.0),
                    RestauranteCombo(2, "Burrito Grande", 5000.0),
                    RestauranteCombo(3, "Quesadilla Especial", 6000.0)
                )
            ),
            Restaurante(
                id = UUID.randomUUID().toString(),
                cedulaJuridica = "3001112223",
                nombre = "Sushi House",
                direccion = "Alajuela, Costa Rica",
                tipoComida = "Japonesa",
                contrasena = "password1234",
                combos = listOf(
                    RestauranteCombo(1, "Sushi Básico", 4000.0),
                    RestauranteCombo(2, "Sushi Combinado", 5000.0),
                    RestauranteCombo(3, "Sushi Premium", 6000.0),
                    RestauranteCombo(4, "Sushi Deluxe", 7000.0)
                )
            )
        )
        saveRestaurantes(mockRestaurants)
    }

    private suspend fun initializeMockPedidos() = withContext(Dispatchers.IO) {
        val clientes = getClientes()
        val restaurantes = getRestaurantes()
        val repartidores = getRepartidores()
        if (clientes.isEmpty() || restaurantes.isEmpty() || repartidores.isEmpty()) return@withContext

        val mockPedidos = listOf(
            Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = clientes[0].id,
                restauranteId = restaurantes[0].id,
                repartidorId = repartidores[0].id,
                combos = listOf(
                    PedidoCombo(1, "Pizza Margherita", 4000.0),
                    PedidoCombo(2, "Pizza Pepperoni", 5000.0)
                ),
                precio = 9000.0,
                distancia = 5.0,
                costoTransporte = 5.0 * 1000.0,
                iva = 9000.0 * 0.13,
                total = 9000.0 + (5.0 * 1000.0) + (9000.0 * 0.13),
                estado = "en preparación",
                horaRealizado = "2025-06-18 12:00:00",
                horaEntregado = null,
                nombreRestaurante = restaurantes[0].nombre
            ),
            Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = clientes[1].id,
                restauranteId = restaurantes[1].id,
                repartidorId = repartidores[1].id,
                combos = listOf(
                    PedidoCombo(1, "Taco al Pastor", 4000.0),
                    PedidoCombo(2, "Burrito Grande", 5000.0)
                ),
                precio = 9000.0,
                distancia = 3.0,
                costoTransporte = 3.0 * 1000.0,
                iva = 9000.0 * 0.13,
                total = 9000.0 + (3.0 * 1000.0) + (9000.0 * 0.13),
                estado = "en camino",
                horaRealizado = "2025-06-18 13:00:00",
                horaEntregado = null,
                nombreRestaurante = restaurantes[1].nombre
            ),
            Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = clientes[2].id,
                restauranteId = restaurantes[0].id,
                repartidorId = repartidores[0].id,
                combos = listOf(
                    PedidoCombo(3, "Pizza Suprema", 6000.0)
                ),
                precio = 6000.0,
                distancia = 4.0,
                costoTransporte = 4.0 * 1000.0,
                iva = 6000.0 * 0.13,
                total = 6000.0 + (4.0 * 1000.0) + (6000.0 * 0.13),
                estado = "entregado",
                horaRealizado = "2025-06-18 14:00:00",
                horaEntregado = "2025-06-18 14:30:00",
                nombreRestaurante = restaurantes[0].nombre
            ),
            Pedido(
                id = UUID.randomUUID().toString(),
                clienteId = clientes[0].id,
                restauranteId = restaurantes[2].id,
                repartidorId = repartidores[1].id,
                combos = listOf(
                    PedidoCombo(1, "Sushi Básico", 4000.0),
                    PedidoCombo(4, "Sushi Deluxe", 7000.0)
                ),
                precio = 11000.0,
                distancia = 6.0,
                costoTransporte = 6.0 * 1000.0,
                iva = 11000.0 * 0.13,
                total = 11000.0 + (6.0 * 1000.0) + (11000.0 * 0.13),
                estado = "en preparación",
                horaRealizado = "2025-06-18 15:00:00",
                horaEntregado = null,
                nombreRestaurante = restaurantes[2].nombre
            )
        )
        savePedidos(mockPedidos)
    }

    private suspend fun initializeMockRepartidores() = withContext(Dispatchers.IO) {
        val mockRepartidores = listOf(
            Repartidor(
                id = "1",
                cedula = "111222333",
                nombre = "Juan Pérez",
                direccion = "Heredia, Costa Rica",
                telefono = "88888888",
                correo = "juan@example.com",
                estado = "disponible",
                kmRecorridosDiarios = 0.0,
                costoPorKmHabiles = 1000.0,
                costoPorKmFeriados = 1500.0,
                amonestaciones = 0,
                quejas = emptyList(),
                contrasena = "password1234",
                numeroTarjeta = "1234567812345678"
            ),
            Repartidor(
                id = "2",
                cedula = "444555666",
                nombre = "María Gómez",
                direccion = "Alajuela, Costa Rica",
                telefono = "77777777",
                correo = "maria@example.com",
                estado = "disponible",
                kmRecorridosDiarios = 0.0,
                costoPorKmHabiles = 1000.0,
                costoPorKmFeriados = 1500.0,
                amonestaciones = 1,
                quejas = listOf("Retraso en entrega"),
                contrasena = "password1234",
                numeroTarjeta = null
            ),
            Repartidor(
                id = "3",
                cedula = "777888999",
                nombre = "Pedro Sánchez",
                direccion = "San José, Costa Rica",
                telefono = "66666666",
                correo = "pedro@example.com",
                estado = "inactivo",
                kmRecorridosDiarios = 0.0,
                costoPorKmHabiles = 1000.0,
                costoPorKmFeriados = 1500.0,
                amonestaciones = 4,
                quejas = listOf(
                    "Mala atención",
                    "Producto dañado",
                    "Retraso",
                    "No entregó factura"
                ),
                contrasena = "password1234",
                numeroTarjeta = null
            )
        )
        saveRepartidores(mockRepartidores)
    }

    private suspend fun initializeMockAdmins() = withContext(Dispatchers.IO) {
        val mockAdmins = listOf(
            Admin(
                id = UUID.randomUUID().toString(),
                nombreUsuario = "admin1234",
                contrasena = "password1234"
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

sealed class AuthResult {
    data class Success(val user: UserType) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
