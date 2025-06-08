package com.example.cletaeatsapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: CletaEatsRepository
) : ViewModel() {
    var cedula = mutableStateOf("")
    var nombre = mutableStateOf("")
    var direccion = mutableStateOf("")
    var telefono = mutableStateOf("")
    var correo = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)
    var isRegistering = mutableStateOf(false)

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val clientes = repository.getClientes()
            val cliente = clientes.find { it.cedula == cedula.value }
            when {
                cliente == null -> {
                    errorMessage.value = "Cliente no registrado. Por favor, regístrese."
                    isRegistering.value = true
                }
                cliente.estado == "suspendido" -> {
                    errorMessage.value = "Su cuenta está suspendida."
                }
                cliente.estado == "activo" -> {
                    errorMessage.value = null
                    onSuccess()
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (cedula.value.isBlank() || nombre.value.isBlank() || direccion.value.isBlank() ||
                telefono.value.isBlank() || correo.value.isBlank()) {
                errorMessage.value = "Por favor, complete todos los campos."
                return@launch
            }
            val cliente = Cliente(
                id = UUID.randomUUID().toString(),
                cedula = cedula.value,
                nombre = nombre.value,
                direccion = direccion.value,
                telefono = telefono.value,
                correo = correo.value,
                estado = "activo"
            )
            if (repository.registerCliente(cliente)) {
                errorMessage.value = null
                onSuccess()
            } else {
                errorMessage.value = "Cédula ya registrada."
            }
        }
    }
}
