package com.example.cletaeatsapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RestauranteRegistroViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var cedulaJuridica = mutableStateOf("")
    var nombre = mutableStateOf("")
    var direccion = mutableStateOf("")
    var tipoComida = mutableStateOf("")
    var contrasena = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d(
                "RestaurantRegistrationViewModel",
                "Starting registration with cedulaJuridica: ${cedulaJuridica.value}"
            )
            isLoading.value = true
            errorMessage.value = null
            if (cedulaJuridica.value.isBlank() || nombre.value.isBlank() || direccion.value.isBlank() || tipoComida.value.isBlank()) {
                Log.e("RestaurantRegistrationViewModel", "Incomplete fields")
                errorMessage.value = "Por favor, complete todos los campos."
                isLoading.value = false
                return@launch
            }
            if (!isCedulaJuridicaValid(cedulaJuridica.value)) {
                Log.e(
                    "RestaurantRegistrationViewModel",
                    "Invalid cedulaJuridica: ${cedulaJuridica.value}"
                )
                errorMessage.value = "Cédula jurídica debe tener 10 dígitos."
                isLoading.value = false
                return@launch
            }
            if (!isAddressValid(direccion.value)) {
                Log.e("RestaurantRegistrationViewModel", "Invalid address: ${direccion.value}")
                errorMessage.value = "Dirección debe tener al menos 10 caracteres."
                isLoading.value = false
                return@launch
            }
            try {
                withTimeout(5000L) {
                    Log.d("RestaurantRegistrationViewModel", "Registering new restaurante")
                    val restaurante = Restaurante(
                        id = UUID.randomUUID().toString(),
                        cedulaJuridica = cedulaJuridica.value,
                        nombre = nombre.value,
                        direccion = direccion.value,
                        tipoComida = tipoComida.value,
                        contrasena = contrasena.value
                    )
                    val restaurantes = repository.getRestaurantes()
                    if (restaurantes.any { it.cedulaJuridica == cedulaJuridica.value }) {
                        Log.w(
                            "RestaurantRegistrationViewModel",
                            "CedulaJuridica already registered: ${cedulaJuridica.value}"
                        )
                        errorMessage.value = "Cédula jurídica ya registrada."
                    } else {
                        Log.d("RestaurantRegistrationViewModel", "Saving new restaurante")
                        repository.saveRestaurantes(restaurantes + restaurante)
                        errorMessage.value = null
                        onSuccess()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("RestaurantRegistrationViewModel", "Registration timed out", e)
                errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
            } catch (e: Exception) {
                Log.e("RestaurantRegistrationViewModel", "Registration failed", e)
                errorMessage.value = "Error al registrar: ${e.message}"
            } finally {
                isLoading.value = false
                Log.d("RestaurantRegistrationViewModel", "Registration completed, isLoading: false")
            }
        }
    }

    private fun isCedulaJuridicaValid(cedula: String): Boolean {
        return cedula.length == 10 && cedula.all { it.isDigit() }
    }

    private fun isAddressValid(address: String): Boolean {
        return address.length >= 10
    }
}
