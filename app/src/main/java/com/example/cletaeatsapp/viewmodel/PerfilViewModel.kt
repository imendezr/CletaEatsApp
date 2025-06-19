package com.example.cletaeatsapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors = _fieldErrors.asStateFlow()

    fun updateProfile(
        nombre: String,
        direccion: String,
        telefono: String,
        correo: String,
        numeroTarjeta: String? = null,
        costoPorKmHabiles: Double? = null,
        costoPorKmFeriados: Double? = null,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        userType: UserType,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            validateFields(
                nombre, direccion, telefono, correo, numeroTarjeta,
                costoPorKmHabiles, costoPorKmFeriados, userType, newPassword, confirmPassword
            )?.let {
                _errorMessage.value = it
                _isLoading.value = false
                return@launch
            }
            try {
                val cedula = when (userType) {
                    is UserType.ClienteUser -> userType.cliente.cedula
                    is UserType.RepartidorUser -> userType.repartidor.cedula
                    is UserType.RestauranteUser -> userType.restaurante.cedulaJuridica
                    else -> ""
                }
                val authenticated = repository.authenticateUser(cedula, currentPassword)
                if (authenticated == null) {
                    _errorMessage.value = "Contraseña actual incorrecta."
                    _fieldErrors.value = mapOf("currentPassword" to "Contraseña actual incorrecta.")
                    return@launch
                }
                val success = when (userType) {
                    is UserType.ClienteUser -> {
                        val updatedCliente = userType.cliente.copy(
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            correo = correo,
                            contrasena = if (newPassword.isNotBlank() && newPassword == confirmPassword) newPassword else currentPassword,
                            numeroTarjeta = numeroTarjeta ?: userType.cliente.numeroTarjeta
                        )
                        repository.updateClienteProfile(updatedCliente)
                    }

                    is UserType.RepartidorUser -> {
                        val updatedRepartidor = userType.repartidor.copy(
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            correo = correo,
                            costoPorKmHabiles = costoPorKmHabiles
                                ?: userType.repartidor.costoPorKmHabiles,
                            costoPorKmFeriados = costoPorKmFeriados
                                ?: userType.repartidor.costoPorKmFeriados,
                            contrasena = if (newPassword.isNotBlank() && newPassword == confirmPassword) newPassword else currentPassword,
                            numeroTarjeta = numeroTarjeta ?: userType.repartidor.numeroTarjeta
                        )
                        repository.updateRepartidorProfile(updatedRepartidor)
                    }

                    is UserType.RestauranteUser -> {
                        val updatedRestaurante = userType.restaurante.copy(
                            nombre = nombre,
                            direccion = direccion,
                            contrasena = if (newPassword.isNotBlank() && newPassword == confirmPassword) newPassword else currentPassword
                        )
                        repository.updateRestauranteProfile(updatedRestaurante)
                    }

                    else -> false
                }
                if (success) {
                    _errorMessage.value = ""
                    _fieldErrors.value = emptyMap()
                    onSuccess()
                } else {
                    _errorMessage.value = "Error al actualizar el perfil."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateFields(
        nombre: String, direccion: String, telefono: String, correo: String,
        numeroTarjeta: String?, costoPorKmHabiles: Double?, costoPorKmFeriados: Double?,
        userType: UserType, newPassword: String, confirmPassword: String
    ): String? {
        val errors = mutableMapOf<String, String>()
        if (nombre.isBlank()) errors["nombre"] = "Nombre es obligatorio."
        else if (!isNameValid(nombre)) errors["nombre"] =
            "Nombre debe tener al menos 2 caracteres y solo letras o espacios."
        if (direccion.isBlank()) errors["direccion"] = "Dirección es obligatoria."
        else if (!isAddressValid(direccion)) errors["direccion"] =
            "Dirección debe tener al menos 10 caracteres."
        if (telefono.isNotBlank() && !isPhoneValid(telefono)) errors["telefono"] =
            "Teléfono debe tener 8 dígitos numéricos."
        if (correo.isNotBlank() && !isEmailValid(correo)) errors["correo"] =
            "Formato de correo inválido."
        if (userType is UserType.ClienteUser && (numeroTarjeta == null || numeroTarjeta.isBlank())) {
            errors["numeroTarjeta"] = "Número de tarjeta es obligatorio para clientes."
        } else if (numeroTarjeta?.isNotBlank() == true && !isNumeroTarjetaValid(numeroTarjeta)) {
            errors["numeroTarjeta"] = "Número de tarjeta debe tener 16 dígitos numéricos."
        }
        if (userType is UserType.RepartidorUser) {
            if (costoPorKmHabiles == null || costoPorKmHabiles <= 0) {
                errors["costoPorKmHabiles"] = "Costo por km hábiles debe ser un número positivo."
            }
            if (costoPorKmFeriados == null || costoPorKmFeriados <= 0) {
                errors["costoPorKmFeriados"] = "Costo por km feriados debe ser un número positivo."
            }
        }
        if (newPassword.isNotBlank() && newPassword != confirmPassword) {
            errors["confirmPassword"] = "Las contraseñas no coinciden."
        } else if (newPassword.isNotBlank() && !isContrasenaValid(newPassword)) {
            errors["newPassword"] = "La nueva contraseña debe tener al menos 8 caracteres."
        }
        _fieldErrors.value = errors
        return errors.values.firstOrNull()
    }

    private fun isEmailValid(email: String) =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isPhoneValid(phone: String) = phone.length == 8 && phone.all { it.isDigit() }
    private fun isAddressValid(address: String) = address.length >= 10
    private fun isNameValid(name: String) =
        name.length >= 2 && name.all { it.isLetter() || it.isWhitespace() }

    private fun isContrasenaValid(password: String) = password.length >= 8
    private fun isNumeroTarjetaValid(numeroTarjeta: String) =
        numeroTarjeta.length == 16 && numeroTarjeta.all { it.isDigit() }
}