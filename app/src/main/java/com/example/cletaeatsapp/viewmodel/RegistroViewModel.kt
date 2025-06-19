package com.example.cletaeatsapp.viewmodel

import android.util.Patterns
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Cliente
import com.example.cletaeatsapp.data.model.Repartidor
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.data.repository.CletaEatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repository: CletaEatsRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _cedula = MutableStateFlow("")
    val cedula = _cedula.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion = _direccion.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono = _telefono.asStateFlow()

    private val _correo = MutableStateFlow("")
    val correo = _correo.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena = _contrasena.asStateFlow()

    private val _distancia = MutableStateFlow("")
    val distancia = _distancia.asStateFlow()

    private val _costoPorKm = MutableStateFlow("")
    val costoPorKm = _costoPorKm.asStateFlow()

    private val _tipoUsuario = MutableStateFlow("Cliente")
    val tipoUsuario = _tipoUsuario.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors = _fieldErrors.asStateFlow()

    fun updateCedula(newCedula: String) {
        _cedula.value = newCedula
        validateFields()
    }

    fun updateNombre(newNombre: String) {
        _nombre.value = newNombre
        validateFields()
    }

    fun updateDireccion(newDireccion: String) {
        _direccion.value = newDireccion
        validateFields()
    }

    fun updateTelefono(newTelefono: String) {
        _telefono.value = newTelefono
        validateFields()
    }

    fun updateCorreo(newCorreo: String) {
        _correo.value = newCorreo
        validateFields()
    }

    fun updateContrasena(newContrasena: String) {
        _contrasena.value = newContrasena
        validateFields()
    }

    fun updateDistancia(newDistancia: String) {
        _distancia.value = newDistancia
        validateFields()
    }

    fun updateCostoPorKm(newCostoPorKm: String) {
        _costoPorKm.value = newCostoPorKm
        validateFields()
    }

    fun updateTipoUsuario(newTipoUsuario: String) {
        _tipoUsuario.value = newTipoUsuario
        validateFields()
    }

    fun register(onSuccess: (String, UserType) -> Unit) {
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
                    when (_tipoUsuario.value) {
                        "Repartidor" -> {
                            val repartidor = Repartidor(
                                id = UUID.randomUUID().toString(),
                                cedula = _cedula.value,
                                nombre = _nombre.value,
                                direccion = _direccion.value,
                                telefono = _telefono.value,
                                correo = _correo.value,
                                estado = "disponible",
                                distancia = _distancia.value.toDoubleOrNull() ?: 0.0,
                                costoPorKm = _costoPorKm.value.toDoubleOrNull() ?: 1000.0,
                                amonestaciones = 0,
                                quejas = emptyList(),
                                contrasena = _contrasena.value
                            )
                            if (repository.registerRepartidor(repartidor)) {
                                val userType = UserType.RepartidorUser(repartidor)
                                saveCedula(_cedula.value)
                                _errorMessage.value = ""
                                _fieldErrors.value = emptyMap()
                                onSuccess(_cedula.value, userType)
                            } else {
                                _errorMessage.value = "Cédula ya registrada."
                                _fieldErrors.value = mapOf("cedula" to "Cédula ya registrada.")
                            }
                        }

                        else -> {
                            val cliente = Cliente(
                                id = UUID.randomUUID().toString(),
                                cedula = _cedula.value,
                                nombre = _nombre.value,
                                direccion = _direccion.value,
                                telefono = _telefono.value,
                                correo = _correo.value,
                                estado = "activo",
                                contrasena = _contrasena.value
                            )
                            if (repository.registerCliente(cliente)) {
                                val userType = UserType.ClienteUser(cliente)
                                saveCedula(_cedula.value)
                                _errorMessage.value = ""
                                _fieldErrors.value = emptyMap()
                                onSuccess(_cedula.value, userType)
                            } else {
                                _errorMessage.value = "Cédula ya registrada."
                                _fieldErrors.value = mapOf("cedula" to "Cédula ya registrada.")
                            }
                        }
                    }
                }
            } catch (_: TimeoutCancellationException) {
                _errorMessage.value = "Tiempo de espera agotado. Intente de nuevo."
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateFields(): String? {
        val errors = mutableMapOf<String, String>()
        if (_cedula.value.isBlank()) errors["cedula"] = "Cédula es obligatoria."
        else if (_cedula.value != "admin" && !isCedulaValid(_cedula.value)) errors["cedula"] =
            "Cédula debe tener 9 dígitos numéricos."
        if (_nombre.value.isBlank()) errors["nombre"] = "Nombre es obligatorio."
        else if (!isNameValid(_nombre.value)) errors["nombre"] =
            "Nombre debe tener al menos 2 caracteres y solo letras o espacios."
        if (_direccion.value.isBlank()) errors["direccion"] = "Dirección es obligatoria."
        else if (!isAddressValid(_direccion.value)) errors["direccion"] =
            "Dirección debe tener al menos 10 caracteres."
        if (_telefono.value.isBlank()) errors["telefono"] = "Teléfono es obligatorio."
        else if (!isPhoneValid(_telefono.value)) errors["telefono"] =
            "Teléfono debe tener 8 dígitos numéricos."
        if (_correo.value.isBlank()) errors["correo"] = "Correo es obligatorio."
        else if (!isEmailValid(_correo.value)) errors["correo"] = "Formato de correo inválido."
        if (_tipoUsuario.value == "Repartidor") {
            if (_distancia.value.isBlank()) errors["distancia"] = "Distancia es obligatoria."
            else if (_distancia.value.toDoubleOrNull() == null || _distancia.value.toDouble() <= 0) errors["distancia"] =
                "Distancia debe ser un número positivo."
            if (_costoPorKm.value.isBlank()) errors["costoPorKm"] = "Costo por km es obligatorio."
            else if (_costoPorKm.value.toDoubleOrNull() == null || _costoPorKm.value.toDouble() <= 0) errors["costoPorKm"] =
                "Costo por km debe ser un número positivo."
        }
        if (_contrasena.value.isBlank()) errors["contrasena"] = "Contraseña es obligatoria."
        else if (!isContrasenaValid(_contrasena.value)) errors["contrasena"] =
            "Contraseña debe tener al menos 8 caracteres."
        _fieldErrors.value = errors
        return errors.values.firstOrNull()
    }

    private fun saveCedula(cedula: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("cedula")] = cedula
            }
        }
    }

    // Reutiliza las validaciones existentes
    private fun isCedulaValid(cedula: String) = cedula.length == 9 && cedula.all { it.isDigit() }
    private fun isEmailValid(email: String) =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isPhoneValid(phone: String) = phone.length == 8 && phone.all { it.isDigit() }
    private fun isAddressValid(address: String) = address.length >= 10
    private fun isNameValid(name: String) =
        name.length >= 2 && name.all { it.isLetter() || it.isWhitespace() }

    private fun isContrasenaValid(password: String) = password.length >= 8
}
