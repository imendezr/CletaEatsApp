package com.example.cletaeatsapp.data.network

import com.example.cletaeatsapp.data.model.Admin
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.model.PedidoCombo
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.model.UserType
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CletaEatsApiService {
    // Client Management
    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

    @POST("clientes/register")
    suspend fun registerCliente(@Body cliente: Cliente): Boolean

    @PUT("clientes/{cedula}")
    suspend fun updateClienteProfile(
        @Path("cedula") cedula: String,
        @Body cliente: Cliente
    ): Boolean

    @GET("clientes/active")
    suspend fun getActiveClients(): List<Cliente>

    @GET("clientes/suspended")
    suspend fun getSuspendedClients(): List<Cliente>

    // Repartidor Management
    @GET("repartidores")
    suspend fun getRepartidores(): List<Repartidor>

    @POST("repartidores/register")
    suspend fun registerRepartidor(@Body repartidor: Repartidor): Boolean

    @PUT("repartidores/{cedula}")
    suspend fun updateRepartidorProfile(
        @Path("cedula") cedula: String,
        @Body repartidor: Repartidor
    ): Boolean

    @POST("repartidores/{repartidorId}/quejas")
    suspend fun addQueja(
        @Path("repartidorId") repartidorId: String,
        @Body request: AddQuejaRequest
    ): Boolean

    @POST("repartidores/reset")
    suspend fun resetRepartidoresEstado(): Boolean

    @GET("repartidores/no-amonestations")
    suspend fun getRepartidoresSinAmonestaciones(): List<Repartidor>

    // Restaurante Management
    @GET("restaurantes")
    suspend fun getRestaurantes(): List<Restaurante>

    @POST("restaurantes/register")
    suspend fun registerRestaurante(@Body restaurante: Restaurante): Boolean

    @PUT("restaurantes/{cedulaJuridica}")
    suspend fun updateRestauranteProfile(
        @Path("cedulaJuridica") cedulaJuridica: String,
        @Body restaurante: Restaurante
    ): Boolean

    // Admin Management
    @GET("admins")
    suspend fun getAdmins(): List<Admin>

    // Order Management
    @GET("pedidos")
    suspend fun getPedidos(): List<Pedido>

    @POST("pedidos")
    suspend fun createOrder(@Body request: CreateOrderRequest): Pedido?

    @POST("pedidos/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: UpdateStatusRequest
    ): Boolean

    // Reports
    @GET("reports/revenue/restaurant")
    suspend fun getTotalRevenueByRestaurant(): Map<String, Double>

    @GET("reports/restaurant/most-orders")
    suspend fun getRestauranteConMasPedidos(): Restaurante?

    @GET("reports/restaurant/least-orders")
    suspend fun getRestauranteConMenosPedidos(): Restaurante?

    @GET("reports/repartidor/quejas")
    suspend fun getQuejasPorRepartidor(): Map<String, List<String>>

    @GET("reports/cliente/pedidos")
    suspend fun getPedidosPorCliente(): Map<String, List<Pedido>>

    @GET("reports/cliente/most-orders")
    suspend fun getClienteConMasPedidos(): Cliente?

    @GET("reports/pedidos/hora-pico")
    suspend fun getHoraPicoPedidos(): String?

    // Authentication
    @POST("auth/login")
    suspend fun authenticateUser(@Body request: AuthRequest): AuthResult
}

data class CreateOrderRequest(
    val clienteId: String,
    val restauranteId: String,
    val combos: List<PedidoCombo>,
    val distancia: Double
)

data class UpdateStatusRequest(
    val newStatus: String,
    val userType: String,
    val userId: String
)

data class AddQuejaRequest(
    val queja: String,
    val addAmonestacion: Boolean
)

data class AuthRequest(
    val cedula: String,
    val contrasena: String
)

sealed class AuthResult {
    @JsonClass(generateAdapter = true)
    data class Success(val user: UserType) : AuthResult()

    @JsonClass(generateAdapter = true)
    data class Error(val message: String) : AuthResult()
}
