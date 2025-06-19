package com.example.cletaeatsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    onRegisterSuccess: (String, UserType) -> Unit,
    registroViewModel: RegistroViewModel = hiltViewModel()
) {
    val cedula by registroViewModel.cedula.collectAsStateWithLifecycle()
    val nombre by registroViewModel.nombre.collectAsStateWithLifecycle()
    val direccion by registroViewModel.direccion.collectAsStateWithLifecycle()
    val telefono by registroViewModel.telefono.collectAsStateWithLifecycle()
    val correo by registroViewModel.correo.collectAsStateWithLifecycle()
    val contrasena by registroViewModel.contrasena.collectAsStateWithLifecycle()
    val numeroTarjeta by registroViewModel.numeroTarjeta.collectAsStateWithLifecycle()
    val costoPorKmHabiles by registroViewModel.costoPorKmHabiles.collectAsStateWithLifecycle()
    val costoPorKmFeriados by registroViewModel.costoPorKmFeriados.collectAsStateWithLifecycle()
    val tipoUsuario by registroViewModel.tipoUsuario.collectAsStateWithLifecycle()
    val isLoading by registroViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by registroViewModel.errorMessage.collectAsStateWithLifecycle()
    val fieldErrors by registroViewModel.fieldErrors.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val padding = if (isExpanded) 32.dp else 16.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(padding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Registrarse",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "loading_transition"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cedula,
                            onValueChange = registroViewModel::updateCedula,
                            label = { Text("Cédula") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["cedula"] != null,
                            supportingText = {
                                fieldErrors["cedula"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = registroViewModel::updateNombre,
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["nombre"] != null,
                            supportingText = {
                                fieldErrors["nombre"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = registroViewModel::updateDireccion,
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["direccion"] != null,
                            supportingText = {
                                fieldErrors["direccion"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = registroViewModel::updateTelefono,
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["telefono"] != null,
                            supportingText = {
                                fieldErrors["telefono"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = correo,
                            onValueChange = registroViewModel::updateCorreo,
                            label = { Text("Correo") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["correo"] != null,
                            supportingText = {
                                fieldErrors["correo"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = numeroTarjeta,
                            onValueChange = registroViewModel::updateNumeroTarjeta,
                            label = { Text(if (tipoUsuario == "Cliente") "Número de Tarjeta" else "Número de Tarjeta (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["numeroTarjeta"] != null,
                            supportingText = {
                                fieldErrors["numeroTarjeta"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        DropdownMenuBox(
                            options = listOf("Cliente", "Repartidor"),
                            selectedOption = tipoUsuario,
                            onOptionSelected = registroViewModel::updateTipoUsuario,
                            label = "Tipo de usuario",
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (tipoUsuario == "Repartidor") {
                            OutlinedTextField(
                                value = costoPorKmHabiles,
                                onValueChange = registroViewModel::updateCostoPorKmHabiles,
                                label = { Text("Costo por km (Hábiles)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = fieldErrors["costoPorKmHabiles"] != null,
                                supportingText = {
                                    fieldErrors["costoPorKmHabiles"]?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = costoPorKmFeriados,
                                onValueChange = registroViewModel::updateCostoPorKmFeriados,
                                label = { Text("Costo por km (Feriados)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = fieldErrors["costoPorKmFeriados"] != null,
                                supportingText = {
                                    fieldErrors["costoPorKmFeriados"]?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = registroViewModel::updateContrasena,
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = fieldErrors["contrasena"] != null,
                            supportingText = {
                                fieldErrors["contrasena"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                        errorMessage.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(
                            onClick = { registroViewModel.register(onRegisterSuccess) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("Registrarse")
                        }
                        TextButton(
                            onClick = { navController.popBackStack() },
                            enabled = !isLoading
                        ) {
                            Text("Volver al inicio de sesión")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Desplegar menú",
                    modifier = Modifier.clickable { expanded = true }
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
