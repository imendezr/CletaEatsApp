package com.example.cletaeatsapp.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.network.AuthResult
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    internal val repository: CletaEatsRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _cedula = MutableStateFlow("")
    val cedulaFlow = _cedula.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena = _contrasena.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors = _fieldErrors.asStateFlow()

    private val _userType = MutableStateFlow<UserType?>(null)
    val userType = _userType.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId = _userId.asStateFlow()

    init {
        loadUserData()
    }

    fun updateCedula(newCedula: String) {
        _cedula.value = newCedula
        // Validación local básica
        _fieldErrors.value = validateFieldsLocally()
    }

    fun updateContrasena(newContrasena: String) {
        _contrasena.value = newContrasena
        // Validación local básica
        _fieldErrors.value = validateFieldsLocally()
    }

    fun saveUserId(id: String) {
        _userId.value = id
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("user_id")] = id
            }
        }
    }

    fun login(onLoginSuccess: (String, UserType) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            val localErrors = validateFieldsLocally()
            if (localErrors.isNotEmpty()) {
                _fieldErrors.value = localErrors
                _isLoading.value = false
                return@launch
            }
            try {
                withTimeout(6000L) {
                    when (val result =
                        repository.authenticateUser(_cedula.value, _contrasena.value)) {
                        is AuthResult.Success -> {
                            val user = result.user
                            saveCedula(_cedula.value)
                            saveUserId(
                                when (user) {
                                    is UserType.ClienteUser -> user.cliente.id
                                    is UserType.RepartidorUser -> user.repartidor.id
                                    is UserType.RestauranteUser -> user.restaurante.id
                                    is UserType.AdminUser -> user.admin.id
                                }
                            )
                            _userType.value = user
                            _errorMessage.value = ""
                            _fieldErrors.value = emptyMap()
                            onLoginSuccess(_cedula.value, user)
                        }

                        is AuthResult.Error -> {
                            _errorMessage.value = result.message
                            _fieldErrors.value = mapOf("general" to result.message)
                        }
                    }
                }
            } catch (_: TimeoutCancellationException) {
                _errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
                _fieldErrors.value =
                    mapOf("general" to "Tiempo de espera agotado. Intente de nuevo.")
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar sesión: ${e.message}"
                _fieldErrors.value = mapOf("general" to "Error al iniciar sesión: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("cedula")] ?: ""
            }.collect { savedCedula ->
                if (savedCedula.isNotBlank()) {
                    _cedula.value = savedCedula
                }
            }
        }
    }

    fun saveCedula(cedula: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("cedula")] = cedula
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey("cedula"))
                preferences.remove(stringPreferencesKey("user_id"))
            }
            _cedula.value = ""
            _userId.value = ""
            _userType.value = null
            _errorMessage.value = ""
            _fieldErrors.value = emptyMap()
        }
    }

    private fun validateFieldsLocally(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (_cedula.value.isBlank()) {
            errors["cedula"] = "Cédula es obligatoria."
        } else if (!isCedulaValid(_cedula.value)) {
            errors["cedula"] = "Cédula debe tener 9 dígitos numéricos."
        }
        if (_contrasena.value.isBlank()) {
            errors["contrasena"] = "Contraseña es obligatoria."
        } else if (!isContrasenaValid(_contrasena.value)) {
            errors["contrasena"] = "Contraseña debe tener al menos 8 caracteres."
        }
        return errors
    }

    private fun isCedulaValid(cedula: String) = cedula.length == 9 && cedula.all { it.isDigit() }
    private fun isContrasenaValid(password: String) = password.length >= 8
}
