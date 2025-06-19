package com.example.cletaeatsapp.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.UserType
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
    val cedula = _cedula.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena = _contrasena.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors = _fieldErrors.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Login)

    private val _cedulaFlow = MutableStateFlow("")
    val cedulaFlow = _cedulaFlow.asStateFlow()

    private val _userType = MutableStateFlow<UserType?>(null)
    val userType = _userType.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data
                .map { it[stringPreferencesKey("cedula")] ?: "" }
                .collect { storedCedula ->
                    _cedulaFlow.value = storedCedula
                    _cedula.value = storedCedula
                    if (storedCedula.isNotBlank()) loadUserData()
                }
        }
    }

    fun updateCedula(newCedula: String) {
        _cedula.value = newCedula
        _cedulaFlow.value = newCedula
        validateFields()
    }

    fun updateContrasena(newContrasena: String) {
        _contrasena.value = newContrasena
        validateFields()
    }

    fun login(onSuccess: (String, UserType) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            validateFields()?.let {
                _errorMessage.value = it
                _isLoading.value = false
                return@launch
            }
            try {
                withTimeout(5000L) {
                    val userType = repository.authenticateUser(_cedula.value, _contrasena.value)
                    when (userType) {
                        is UserType.ClienteUser -> {
                            if (userType.cliente.estado == "suspendido") {
                                _errorMessage.value = "Su cuenta está suspendida."
                            } else {
                                _uiState.value = LoginUiState.Authenticated
                                _userType.value = userType
                                saveCedula(_cedula.value)
                                _errorMessage.value = ""
                                _fieldErrors.value = emptyMap()
                                onSuccess(_cedula.value, userType)
                            }
                        }
                        is UserType.RepartidorUser -> {
                            if (userType.repartidor.estado == "inactivo") {
                                _errorMessage.value = "Su cuenta está inactiva."
                            } else {
                                _uiState.value = LoginUiState.Authenticated
                                _userType.value = userType
                                saveCedula(_cedula.value)
                                _errorMessage.value = ""
                                _fieldErrors.value = emptyMap()
                                onSuccess(_cedula.value, userType)
                            }
                        }
                        is UserType.RestauranteUser -> {
                            _uiState.value = LoginUiState.Authenticated
                            _userType.value = userType
                            saveCedula(_cedula.value)
                            _errorMessage.value = ""
                            _fieldErrors.value = emptyMap()
                            onSuccess(_cedula.value, userType)
                        }
                        is UserType.AdminUser -> {
                            _uiState.value = LoginUiState.Authenticated
                            _userType.value = userType
                            saveCedula(_cedula.value)
                            _errorMessage.value = ""
                            _fieldErrors.value = emptyMap()
                            onSuccess(_cedula.value, userType)
                        }
                        null -> {
                            _errorMessage.value = "Usuario no registrado o contraseña incorrecta."
                            _fieldErrors.value = mapOf("contrasena" to "Credenciales inválidas.")
                        }
                    }
                }
            } catch (_: TimeoutCancellationException) {
                _errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar sesión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateFields(): String? {
        val errors = mutableMapOf<String, String>()
        if (_cedula.value.isBlank()) errors["cedula"] = "Cédula o usuario es obligatorio."
        else if (_cedula.value != "admin" && !isCedulaValid(_cedula.value)) errors["cedula"] =
            if (_cedula.value.startsWith("3")) "Cédula jurídica debe tener al menos 10 dígitos numéricos."
            else "Cédula debe tener 9 dígitos numéricos."
        if (_contrasena.value.isBlank()) errors["contrasena"] = "Contraseña es obligatoria."
        _fieldErrors.value = errors
        return errors.values.firstOrNull()
    }

    fun loadUserData() {
        viewModelScope.launch {
            if (_uiState.value != LoginUiState.Authenticated) return@launch
            try {
                _isLoading.value = true
                val userType = repository.authenticateUser(_cedula.value, _contrasena.value)
                if (userType != null) {
                    _userType.value = userType
                } else {
                    _errorMessage.value = "No se encontraron datos del usuario."
                    _uiState.value = LoginUiState.Error
                    triggerNavigationEvent(NavigationEvent.NavigateToLogin)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
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
            }
            _cedula.value = ""
            _contrasena.value = ""
            _errorMessage.value = ""
            _isLoading.value = false
            _fieldErrors.value = emptyMap()
            _uiState.value = LoginUiState.Login
            _userType.value = null
            triggerNavigationEvent(NavigationEvent.NavigateToLogin)
        }
    }

    fun triggerNavigationEvent(event: NavigationEvent) {
        _navigationEvent.value = when (event) {
            is NavigationEvent.NavigateToLogin -> event
            is NavigationEvent.NavigateToClientHome -> event
            is NavigationEvent.NavigateToRepartidorOrders -> event
            is NavigationEvent.NavigateToAdminReports -> event
            is NavigationEvent.NavigateToRestauranteOrders -> event
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    private fun isCedulaValid(cedula: String): Boolean {
        return when {
            cedula == "admin" -> true // Excepción para el admin
            cedula.startsWith("3") -> cedula.length >= 10 && cedula.all { it.isDigit() } // Cédula jurídica
            else -> cedula.length == 9 && cedula.all { it.isDigit() } // Cédula personal
        }
    }
}

sealed class NavigationEvent {
    object NavigateToLogin : NavigationEvent()
    data class NavigateToClientHome(val cedula: String) : NavigationEvent()
    data class NavigateToRepartidorOrders(val cedula: String) : NavigationEvent()
    object NavigateToAdminReports : NavigationEvent()
    data class NavigateToRestauranteOrders(val cedula: String) : NavigationEvent()
}

sealed class LoginUiState {
    object Login : LoginUiState()
    object Authenticated : LoginUiState()
    object Error : LoginUiState()
}
