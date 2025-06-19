package com.example.cletaeatsapp.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cletaeatsapp.data.model.Restaurante
import com.example.cletaeatsapp.data.model.RestauranteCombo
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
class RestauranteRegistroViewModel @Inject constructor(
    private val repository: CletaEatsRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    // Existing state (unchanged)
    private val _cedulaJuridica = MutableStateFlow("")
    val cedulaJuridica = _cedulaJuridica.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre = _nombre.asStateFlow()

    private val _direccion = MutableStateFlow("")
    val direccion = _direccion.asStateFlow()

    private val _tipoComida = MutableStateFlow("")
    val tipoComida = _tipoComida.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena = _contrasena.asStateFlow()

    private val _combos = MutableStateFlow<List<RestauranteCombo>>(emptyList())
    val combos = _combos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors = _fieldErrors.asStateFlow()

    // New state for combo dialog
    private val _comboNumero = MutableStateFlow("")
    val comboNumero = _comboNumero.asStateFlow()

    private val _comboNombre = MutableStateFlow("")
    val comboNombre = _comboNombre.asStateFlow()

    private val _comboPrecio = MutableStateFlow("")
    val comboPrecio = _comboPrecio.asStateFlow()

    private val _comboFieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val comboFieldErrors = _comboFieldErrors.asStateFlow()

    private val _isComboValid = MutableStateFlow(false)
    val isComboValid = _isComboValid.asStateFlow()

    // Update methods for main form (unchanged)
    fun updateCedulaJuridica(newCedula: String) {
        _cedulaJuridica.value = newCedula
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

    fun updateTipoComida(newTipoComida: String) {
        _tipoComida.value = newTipoComida
        validateFields()
    }

    fun updateContrasena(newContrasena: String) {
        _contrasena.value = newContrasena
        validateFields()
    }

    // Update methods for combo dialog
    fun updateComboNumero(newNumero: String) {
        _comboNumero.value = newNumero
        validateComboFields()
    }

    fun updateComboNombre(newNombre: String) {
        _comboNombre.value = newNombre
        validateComboFields()
    }

    fun updateComboPrecio(newPrecio: String) {
        _comboPrecio.value = newPrecio
        validateComboFields()
    }

    fun addCombo() {
        val numero = _comboNumero.value.toIntOrNull()
        val precio = _comboPrecio.value.toDoubleOrNull()
        if (numero != null && precio != null && _isComboValid.value) {
            val newCombo = RestauranteCombo(numero, _comboNombre.value, precio)
            _combos.value = _combos.value + newCombo
            resetComboFields()
            validateFields()
        }
    }

    fun removeCombo(numero: Int) {
        _combos.value = _combos.value.filter { it.numero != numero }
        validateFields()
    }

    fun resetComboFields() {
        _comboNumero.value = ""
        _comboNombre.value = ""
        _comboPrecio.value = ""
        _comboFieldErrors.value = emptyMap()
        _isComboValid.value = false
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
                    val restaurante = Restaurante(
                        id = UUID.randomUUID().toString(),
                        cedulaJuridica = _cedulaJuridica.value,
                        nombre = _nombre.value,
                        direccion = _direccion.value,
                        tipoComida = _tipoComida.value,
                        contrasena = _contrasena.value,
                        combos = _combos.value
                    )
                    if (repository.registerRestaurante(restaurante)) {
                        val userType = UserType.RestauranteUser(restaurante)
                        saveUserId(restaurante.id) // Guardar user_id
                        _errorMessage.value = ""
                        _fieldErrors.value = emptyMap()
                        onSuccess(_cedulaJuridica.value, userType)
                    } else {
                        _errorMessage.value = "Cédula jurídica ya registrada."
                        _fieldErrors.value =
                            mapOf("cedulaJuridica" to "Cédula jurídica ya registrada.")
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

    private fun saveUserId(id: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("user_id")] = id
            }
        }
    }

    private fun validateFields(): String? {
        val errors = mutableMapOf<String, String>()
        if (_cedulaJuridica.value.isBlank()) errors["cedulaJuridica"] =
            "Cédula jurídica es obligatoria."
        else if (!isCedulaJuridicaValid(_cedulaJuridica.value)) errors["cedulaJuridica"] =
            "Cédula jurídica debe tener 10 dígitos numéricos."
        if (_nombre.value.isBlank()) errors["nombre"] = "Nombre es obligatorio."
        else if (!isNameValid(_nombre.value)) errors["nombre"] =
            "Nombre debe tener al menos 2 caracteres y solo letras o espacios."
        if (_direccion.value.isBlank()) errors["direccion"] = "Dirección es obligatoria."
        else if (!isAddressValid(_direccion.value)) errors["direccion"] =
            "Dirección debe tener al menos 10 caracteres."
        if (_tipoComida.value.isBlank()) errors["tipoComida"] = "Tipo de comida es obligatorio."
        else if (_tipoComida.value.length < 3) errors["tipoComida"] =
            "Tipo de comida debe tener al menos 3 caracteres."
        if (_contrasena.value.isBlank()) errors["contrasena"] = "Contraseña es obligatoria."
        else if (!isContrasenaValid(_contrasena.value)) errors["contrasena"] =
            "Contraseña debe tener al menos 8 caracteres."
        if (_combos.value.isEmpty()) errors["combos"] = "Debe registrar al menos un combo."
        else {
            _combos.value.forEach { combo ->
                if (combo.numero !in 1..9) errors["combo_${combo.numero}"] =
                    "Número de combo debe estar entre 1 y 9."
                if (combo.nombre.isBlank()) errors["combo_${combo.numero}_nombre"] =
                    "Nombre del combo es obligatorio."
                if (combo.precio <= 0) errors["combo_${combo.numero}_precio"] =
                    "Precio del combo debe ser positivo."
            }
            // Check for duplicate combo numbers
            val comboNumbers = _combos.value.map { it.numero }
            if (comboNumbers.distinct().size != comboNumbers.size) {
                errors["combos"] = "No se permiten números de combo duplicados."
            }
        }
        _fieldErrors.value = errors
        return errors.values.firstOrNull()
    }

    private fun validateComboFields() {
        val errors = mutableMapOf<String, String>()
        val numero = _comboNumero.value.toIntOrNull()
        if (_comboNumero.value.isBlank()) {
            errors["comboNumero"] = "Número es obligatorio."
        } else if (numero == null || numero !in 1..9) {
            errors["comboNumero"] = "Número debe estar entre 1 y 9."
        } else if (_combos.value.any { it.numero == numero }) {
            errors["comboNumero"] = "Número de combo ya registrado."
        }
        if (_comboNombre.value.isBlank()) {
            errors["comboNombre"] = "Nombre es obligatorio."
        } else if (_comboNombre.value.length < 3) {
            errors["comboNombre"] = "Nombre debe tener al menos 3 caracteres."
        }
        if (_comboPrecio.value.isBlank()) {
            errors["comboPrecio"] = "Precio es obligatorio."
        } else if (_comboPrecio.value.toDoubleOrNull() == null || _comboPrecio.value.toDoubleOrNull()!! <= 0) {
            errors["comboPrecio"] = "Precio debe ser un número positivo."
        }
        _comboFieldErrors.value = errors
        _isComboValid.value = errors.isEmpty()
    }

    private fun isCedulaJuridicaValid(cedula: String) =
        cedula.length == 10 && cedula.all { it.isDigit() }

    private fun isNameValid(name: String) =
        name.length >= 2 && name.all { it.isLetter() || it.isWhitespace() }

    private fun isAddressValid(address: String) = address.length >= 10
    private fun isContrasenaValid(password: String) = password.length >= 8
}
