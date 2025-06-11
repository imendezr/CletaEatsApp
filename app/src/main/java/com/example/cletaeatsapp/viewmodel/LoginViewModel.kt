package com.example.cletaeatsapp.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: CletaEatsRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    var cedula = mutableStateOf("")
        set(value) {
            Log.d("LoginViewModel", "Cedula changed to: ${value.value}")
            field = value
            _cedulaFlow.value = value.value
        }
    var nombre = mutableStateOf("")
    var direccion = mutableStateOf("")
    var telefono = mutableStateOf("")
    var correo = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)
    var isRegistering = mutableStateOf(false)
    var isLoading = mutableStateOf(false)

    private val _cedulaFlow = MutableStateFlow("")
    val cedulaFlow: StateFlow<String> = _cedulaFlow.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data
                .map { preferences -> preferences[stringPreferencesKey("cedula")] ?: "" }
                .collect { storedCedula ->
                    _cedulaFlow.value = storedCedula
                    cedula.value = storedCedula
                    if (storedCedula.isNotBlank()) {
                        loadUserData(storedCedula)
                    }
                }
        }
    }

    fun login(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("LoginViewModel", "Starting login with cedula: ${cedula.value}")
            isLoading.value = true
            errorMessage.value = null
            if (!isCedulaValid(cedula.value)) {
                Log.e("LoginViewModel", "Invalid cedula: ${cedula.value}")
                errorMessage.value = "Cédula debe tener 9 dígitos."
                isLoading.value = false
                return@launch
            }
            try {
                withTimeout(5000L) {
                    Log.d("LoginViewModel", "Fetching clientes from repository")
                    val clientes = repository.getClientes()
                    Log.d("LoginViewModel", "Found ${clientes.size} clientes")
                    val cliente = clientes.find { it.cedula == cedula.value }
                    when {
                        cliente == null -> {
                            Log.w("LoginViewModel", "No cliente found for cedula: ${cedula.value}")
                            errorMessage.value = "Cliente no registrado. Por favor, regístrese."
                            isRegistering.value = true
                        }

                        cliente.estado == "suspendido" -> {
                            Log.w("LoginViewModel", "Cliente suspended: ${cedula.value}")
                            errorMessage.value = "Su cuenta está suspendida."
                        }

                        cliente.estado == "activo" -> {
                            Log.d("LoginViewModel", "Login successful for cedula: ${cedula.value}")
                            updateUserData(cliente)
                            errorMessage.value = null
                            onSuccess(cedula.value)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("LoginViewModel", "Login timed out after 5 seconds", e)
                errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed", e)
                errorMessage.value = "Error al iniciar sesión: ${e.message}"
            } finally {
                isLoading.value = false
                Log.d("LoginViewModel", "Login operation completed, isLoading: false")
            }
        }
    }

    fun register(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("LoginViewModel", "Starting registration with cedula: ${cedula.value}")
            isLoading.value = true
            errorMessage.value = null
            if (cedula.value.isBlank() || nombre.value.isBlank() || direccion.value.isBlank() ||
                telefono.value.isBlank() || correo.value.isBlank()
            ) {
                Log.e("LoginViewModel", "Incomplete fields during registration")
                errorMessage.value = "Por favor, complete todos los campos."
                isLoading.value = false
                return@launch
            }
            if (!isCedulaValid(cedula.value)) {
                Log.e("LoginViewModel", "Invalid cedula: ${cedula.value}")
                errorMessage.value = "Cédula debe tener 9 dígitos."
                isLoading.value = false
                return@launch
            }
            if (!isEmailValid(correo.value)) {
                Log.e("LoginViewModel", "Invalid email: ${correo.value}")
                errorMessage.value = "Formato de correo inválido."
                isLoading.value = false
                return@launch
            }
            if (!isPhoneValid(telefono.value)) {
                Log.e("LoginViewModel", "Invalid phone: ${telefono.value}")
                errorMessage.value = "Teléfono debe tener 8 dígitos."
                isLoading.value = false
                return@launch
            }
            if (!isAddressValid(direccion.value)) {
                Log.e("LoginViewModel", "Invalid address: ${direccion.value}")
                errorMessage.value = "Dirección debe tener al menos 10 caracteres."
                isLoading.value = false
                return@launch
            }
            try {
                withTimeout(5000L) {
                    Log.d("LoginViewModel", "Registering new cliente")
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
                        Log.d(
                            "LoginViewModel",
                            "Registration successful for cedula: ${cedula.value}"
                        )
                        updateUserData(cliente)
                        errorMessage.value = null
                        onSuccess(cedula.value)
                    } else {
                        Log.w("LoginViewModel", "Cedula already registered: ${cedula.value}")
                        errorMessage.value = "Cédula ya registrada."
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("LoginViewModel", "Registration timed out after 5 seconds", e)
                errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Registration failed", e)
                errorMessage.value = "Error al registrar: ${e.message}"
            } finally {
                isLoading.value = false
                Log.d("LoginViewModel", "Registration operation completed, isLoading: false")
            }
        }
    }

    private fun loadUserData(cedula: String) {
        viewModelScope.launch {
            try {
                val clientes = repository.getClientes()
                val cliente = clientes.find { it.cedula == cedula }
                cliente?.let { updateUserData(it) }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Failed to load user data", e)
            }
        }
    }

    private fun updateUserData(cliente: Cliente) {
        nombre.value = cliente.nombre
        direccion.value = cliente.direccion
        telefono.value = cliente.telefono
        correo.value = cliente.correo
    }

    fun saveCedula(cedula: String) {
        viewModelScope.launch {
            Log.d("LoginViewModel", "Saving cedula: $cedula to DataStore")
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("cedula")] = cedula
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("LoginViewModel", "Logging out, clearing DataStore")
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey("cedula"))
            }
            cedula.value = ""
            nombre.value = ""
            direccion.value = ""
            telefono.value = ""
            correo.value = ""
            isRegistering.value = false
            errorMessage.value = null
            isLoading.value = false
            _navigationEvent.value = NavigationEvent.NavigateToLogin
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
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
}

sealed class NavigationEvent {
    data object NavigateToLogin : NavigationEvent()
}
