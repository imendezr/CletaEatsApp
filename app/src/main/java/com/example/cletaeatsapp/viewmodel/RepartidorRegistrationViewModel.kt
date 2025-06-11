package com.example.cletaeatsapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RepartidorRegistrationViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var cedula = mutableStateOf("")
    var nombre = mutableStateOf("")
    var direccion = mutableStateOf("")
    var telefono = mutableStateOf("")
    var correo = mutableStateOf("")
    var distancia = mutableStateOf("")
    var costoPorKm = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(
                "RepartidorRegistrationViewModel",
                "Starting registration with cedula: ${cedula.value}"
            )
            isLoading.value = true
            errorMessage.value = null
            if (cedula.value.isBlank() || nombre.value.isBlank() || direccion.value.isBlank() ||
                telefono.value.isBlank() || correo.value.isBlank() || distancia.value.isBlank() ||
                costoPorKm.value.isBlank()
            ) {
                Log.e("RepartidorRegistrationViewModel", "Incomplete fields")
                errorMessage.value = "Por favor, complete todos los campos."
                isLoading.value = false
                return@launch
            }
            if (!isCedulaValid(cedula.value)) {
                Log.e("RepartidorRegistrationViewModel", "Invalid cedula: ${cedula.value}")
                errorMessage.value = "Cédula debe tener 9 dígitos."
                isLoading.value = false
                return@launch
            }
            if (!isEmailValid(correo.value)) {
                Log.e("RepartidorRegistrationViewModel", "Invalid email: ${correo.value}")
                errorMessage.value = "Formato de correo inválido."
                isLoading.value = false
                return@launch
            }
            if (!isPhoneValid(telefono.value)) {
                Log.e("RepartidorRegistrationViewModel", "Invalid phone: ${telefono.value}")
                errorMessage.value = "Teléfono debe tener 8 dígitos."
                isLoading.value = false
                return@launch
            }
            if (!isAddressValid(direccion.value)) {
                Log.e("RepartidorRegistrationViewModel", "Invalid address: ${direccion.value}")
                errorMessage.value = "Dirección debe tener al menos 10 caracteres."
                isLoading.value = false
                return@launch
            }
            if (!isNumberValid(distancia.value)) {
                Log.e("RepartidorRegistrationViewModel", "Invalid distancia: ${distancia.value}")
                errorMessage.value = "Distancia debe ser un número positivo."
                isLoading.value = false
                return@launch
            }
            if (!isNumberValid(costoPorKm.value)) {
                Log.e("RepartidorRegistrationViewModel", "Invalid costoPorKm: ${costoPorKm.value}")
                errorMessage.value = "Costo por km debe ser un número positivo."
                isLoading.value = false
                return@launch
            }
            try {
                withTimeout(5000L) {
                    Log.d("RepartidorRegistrationViewModel", "Registering new repartidor")
                    val repartidor = Repartidor(
                        id = UUID.randomUUID().toString(),
                        cedula = cedula.value,
                        nombre = nombre.value,
                        direccion = direccion.value,
                        telefono = telefono.value,
                        correo = correo.value,
                        estado = "disponible",
                        distancia = distancia.value.toDouble(),
                        costoPorKm = costoPorKm.value.toDouble(),
                        amonestaciones = 0,
                        quejas = emptyList()
                    )
                    val repartidores = repository.getRepartidores()
                    if (repartidores.any { it.cedula == cedula.value }) {
                        Log.w(
                            "RepartidorRegistrationViewModel",
                            "Cedula already registered: ${cedula.value}"
                        )
                        errorMessage.value = "Cédula ya registrada."
                    } else {
                        Log.d("RepartidorRegistrationViewModel", "Saving new repartidor")
                        repository.saveRepartidores(repartidores + repartidor)
                        errorMessage.value = null
                        onSuccess()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("RepartidorRegistrationViewModel", "Registration timed out", e)
                errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
            } catch (e: Exception) {
                Log.e("RepartidorRegistrationViewModel", "Registration failed", e)
                errorMessage.value = "Error al registrar: ${e.message}"
            } finally {
                isLoading.value = false
                Log.d("RepartidorRegistrationViewModel", "Registration completed")
            }
        }
    }

    private fun isCedulaValid(cedula: String): Boolean {
        return cedula.length == 9 && cedula.all { it.isDigit() }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPhoneValid(phone: String): Boolean {
        return phone.length == 8 && phone.all { it.isDigit() }
    }

    private fun isAddressValid(address: String): Boolean {
        return address.length >= 10
    }

    private fun isNumberValid(value: String): Boolean {
        return try {
            value.toDouble() > 0
        } catch (_: NumberFormatException) {
            false
        }
    }
}
