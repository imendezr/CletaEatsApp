package com.example.cletaeatsapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import com.example.cletaeatsapp.data.repository.UserType
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

    fun updateProfile(
        cedula: String,
        nombre: String,
        direccion: String,
        telefono: String,
        correo: String,
        distancia: Double? = null,
        costoPorKm: Double? = null,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userType =
                    repository.authenticateUser(cedula, currentPassword) // Reauthenticate
                val success = when (userType) {
                    is UserType.ClientUser -> {
                        val updatedCliente = userType.cliente.copy(
                            cedula = cedula,
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            correo = correo,
                            contrasena = if (newPassword.isNotBlank() && newPassword == confirmPassword) newPassword else currentPassword
                        )
                        repository.updateClienteProfile(updatedCliente)
                    }

                    is UserType.RepartidorUser -> {
                        val updatedRepartidor = userType.repartidor.copy(
                            cedula = cedula,
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            correo = correo,
                            distancia = distancia
                                ?: userType.repartidor.distancia,
                            costoPorKm = costoPorKm
                                ?: userType.repartidor.costoPorKm,
                            contrasena = if (newPassword.isNotBlank() && newPassword == confirmPassword) newPassword else currentPassword
                        )
                        repository.updateRepartidorProfile(updatedRepartidor)
                    }

                    else -> false
                }
                if (success) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Error al actualizar el perfil"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
