package com.example.cletaeatsapp.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.repository.AuthResult
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

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId = _userId.asStateFlow()

    init {
        loadUserData()
    }

    fun updateCedula(newCedula: String) {
        _cedula.value = newCedula
        viewModelScope.launch {
            validateFields()
        }
    }

    fun updateContrasena(newContrasena: String) {
        _contrasena.value = newContrasena
        viewModelScope.launch {
            validateFields()
        }
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
            validateFields()?.let {
                _errorMessage.value = it
                _isLoading.value = false
                return@launch
            }
            try {
                withTimeout(6000L) {
                    when (val result =
                        repository.authenticateUser(_cedula.value, _contrasena.value)) {
                        is AuthResult.Success -> {
                            val user = result.user
                            when (user) {
                                is UserType.ClienteUser -> {
                                    saveCedula(_cedula.value)
                                    saveUserId(user.cliente.id)
                                    _userType.value = user
                                    _errorMessage.value = ""
                                    _fieldErrors.value = emptyMap()
                                    onLoginSuccess(_cedula.value, user)
                                    _navigationEvent.value =
                                        NavigationEvent.NavigateToClienteHome(user.cliente.id)
                                }

                                is UserType.RepartidorUser -> {
                                    saveCedula(_cedula.value)
                                    saveUserId(user.repartidor.id)
                                    _userType.value = user
                                    _errorMessage.value = ""
                                    _fieldErrors.value = emptyMap()
                                    onLoginSuccess(_cedula.value, user)
                                    _navigationEvent.value =
                                        NavigationEvent.NavigateToRepartidorHome(user.repartidor.id)
                                }

                                is UserType.RestauranteUser -> {
                                    saveCedula(_cedula.value)
                                    saveUserId(user.restaurante.id)
                                    _userType.value = user
                                    _errorMessage.value = ""
                                    _fieldErrors.value = emptyMap()
                                    onLoginSuccess(_cedula.value, user)
                                    _navigationEvent.value =
                                        NavigationEvent.NavigateToRestauranteOrders(user.restaurante.id)
                                }

                                is UserType.AdminUser -> {
                                    saveCedula(_cedula.value)
                                    saveUserId(user.admin.id)
                                    _userType.value = user
                                    _errorMessage.value = ""
                                    _fieldErrors.value = emptyMap()
                                    onLoginSuccess(_cedula.value, user)
                                    _navigationEvent.value =
                                        NavigationEvent.NavigateToAdminHome(user.admin.id)
                                }
                            }
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
                val cedula = preferences[stringPreferencesKey("cedula")] ?: ""
                val contrasena = preferences[stringPreferencesKey("contrasena")] ?: ""
                Pair(cedula, contrasena)
            }.collect { (savedCedula, savedContrasena) ->
                if (savedCedula.isNotBlank() && savedContrasena.isNotBlank()) {
                    _cedula.value = savedCedula
                    try {
                        when (val result =
                            repository.authenticateUser(savedCedula, savedContrasena)) {
                            is AuthResult.Success -> {
                                val user = result.user
                                _userType.value = user
                                when (user) {
                                    is UserType.ClienteUser -> saveUserId(user.cliente.id)
                                    is UserType.RepartidorUser -> saveUserId(user.repartidor.id)
                                    is UserType.RestauranteUser -> saveUserId(user.restaurante.id)
                                    is UserType.AdminUser -> saveUserId(user.admin.id)
                                }
                            }

                            is AuthResult.Error -> {
                                _userType.value = null
                                clearUserData()
                                _errorMessage.value = result.message
                            }
                        }
                    } catch (e: Exception) {
                        _userType.value = null
                        clearUserData()
                        _errorMessage.value = "Error al cargar datos: ${e.message}"
                    }
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
            _contrasena.value = ""
            _userType.value = null
            _userId.value = ""
            _errorMessage.value = ""
            _fieldErrors.value = emptyMap()
            _navigationEvent.value = NavigationEvent.NavigateToLogin
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    private fun clearUserData() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey("cedula"))
                preferences.remove(stringPreferencesKey("user_id"))
            }
            _cedula.value = ""
            _userId.value = ""
        }
    }

    private suspend fun validateFields(): String? {
        val errors = mutableMapOf<String, String>()
        if (_cedula.value.isBlank()) {
            errors["cedula"] = "Cédula es obligatoria."
        } else {
            val user = repository.getUserByCedula(_cedula.value)
            when (user) {
                is UserType.RestauranteUser -> {
                    if (!isCedulaJuridicaValid(_cedula.value)) {
                        errors["cedula"] = "Cédula jurídica debe tener 10 dígitos numéricos."
                    }
                }

                is UserType.AdminUser -> {
                    if (!isNombreUsuarioValid(_cedula.value)) {
                        errors["cedula"] = "Nombre de usuario debe tener al menos 8 caracteres."
                    }
                }

                else -> {
                    if (!isCedulaValid(_cedula.value)) {
                        errors["cedula"] = "Cédula debe tener 9 dígitos numéricos."
                    }
                }
            }
        }
        if (_contrasena.value.isBlank()) {
            errors["contrasena"] = "Contraseña es obligatoria."
        } else if (!isContrasenaValid(_contrasena.value)) {
            errors["contrasena"] = "Contraseña debe tener al menos 8 caracteres."
        }
        _fieldErrors.value = errors
        return errors.values.firstOrNull()
    }

    private fun isCedulaValid(cedula: String) = cedula.length == 9 && cedula.all { it.isDigit() }
    private fun isCedulaJuridicaValid(cedula: String) =
        cedula.length == 10 && cedula.all { it.isDigit() }

    private fun isNombreUsuarioValid(nombreUsuario: String) = nombreUsuario.length >= 8
    private fun isContrasenaValid(password: String) = password.length >= 8
}

sealed class NavigationEvent {
    data class NavigateToClienteHome(val id: String) : NavigationEvent()
    data class NavigateToRepartidorHome(val id: String) : NavigationEvent()
    data class NavigateToRestauranteOrders(val id: String) : NavigationEvent()
    data class NavigateToAdminHome(val id: String) : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
}
