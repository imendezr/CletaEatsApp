package com.example.cletaeatsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import com.example.cletaeatsapp.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PerfilScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel,
    navController: NavController,
    perfilViewModel: PerfilViewModel = hiltViewModel()
) {
    val cedulaState by loginViewModel.cedulaFlow.collectAsStateWithLifecycle()
    val isLoadingState by loginViewModel.isLoading.collectAsStateWithLifecycle()
    val userTypeState by loginViewModel.userType.collectAsStateWithLifecycle()
    val errorMessage by perfilViewModel.errorMessage.collectAsStateWithLifecycle()
    val fieldErrors by perfilViewModel.fieldErrors.collectAsStateWithLifecycle()
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordChange by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val padding = if (isExpanded) 32.dp else 16.dp

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombre by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClienteUser)?.cliente?.nombre
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.nombre
                ?: (userTypeState as? UserType.RestauranteUser)?.restaurante?.nombre ?: ""
        )
    }
    var direccion by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClienteUser)?.cliente?.direccion
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.direccion
                ?: (userTypeState as? UserType.RestauranteUser)?.restaurante?.direccion ?: ""
        )
    }
    var telefono by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClienteUser)?.cliente?.telefono
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.telefono ?: ""
        )
    }
    var correo by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClienteUser)?.cliente?.correo
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.correo ?: ""
        )
    }
    var numeroTarjeta by remember {
        mutableStateOf(
            (userTypeState as? UserType.ClienteUser)?.cliente?.numeroTarjeta
                ?: (userTypeState as? UserType.RepartidorUser)?.repartidor?.numeroTarjeta ?: ""
        )
    }
    var costoPorKmHabiles by remember {
        mutableStateOf(
            (userTypeState as? UserType.RepartidorUser)?.repartidor?.costoPorKmHabiles?.toString()
                ?: ""
        )
    }
    var costoPorKmFeriados by remember {
        mutableStateOf(
            (userTypeState as? UserType.RepartidorUser)?.repartidor?.costoPorKmFeriados?.toString()
                ?: ""
        )
    }
    var tipoComida by remember {
        mutableStateOf((userTypeState as? UserType.RestauranteUser)?.restaurante?.tipoComida ?: "")
    }

    LaunchedEffect(userTypeState, cedulaState) {
        if (cedulaState.isBlank() || userTypeState == null) {
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = loginViewModel::logout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(padding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = when (userTypeState) {
                    is UserType.ClienteUser -> "Perfil del Cliente"
                    is UserType.RepartidorUser -> "Perfil del Repartidor"
                    is UserType.RestauranteUser -> "Perfil del Restaurante"
                    else -> "Perfil"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            AnimatedContent(
                targetState = isLoadingState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "loading_transition"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (cedulaState.isBlank() || userTypeState == null) {
                    Text(
                        text = "No hay datos de usuario disponibles. Inicie sesión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    val userData = userTypeState!!
                    AnimatedContent(
                        targetState = isEditing,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "profile_edit_transition"
                    ) { editing ->
                        if (editing) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileField(label = "Cédula", value = cedulaState, enabled = false)
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
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
                                    onValueChange = { direccion = it },
                                    label = { Text("Dirección") },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = fieldErrors["direccion"] != null,
                                    supportingText = {
                                        fieldErrors["direccion"]?.let {
                                            Text(it, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                )
                                if (userData is UserType.RestauranteUser) {
                                    ProfileField(
                                        label = "Tipo de Comida",
                                        value = tipoComida,
                                        enabled = false
                                    )
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(userData.restaurante.combos) { combo ->
                                            ProfileField(
                                                label = "Combo ${combo.numero}",
                                                value = "${combo.nombre} - ₡${combo.precio}",
                                                enabled = false
                                            )
                                        }
                                    }
                                }
                                if (userData !is UserType.RestauranteUser) {
                                    OutlinedTextField(
                                        value = telefono,
                                        onValueChange = { telefono = it },
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
                                        onValueChange = { correo = it },
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
                                        onValueChange = { numeroTarjeta = it },
                                        label = { Text(if (userData is UserType.ClienteUser) "Número de Tarjeta" else "Número de Tarjeta (opcional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = fieldErrors["numeroTarjeta"] != null,
                                        supportingText = {
                                            fieldErrors["numeroTarjeta"]?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error)
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                                if (userData is UserType.RepartidorUser) {
                                    OutlinedTextField(
                                        value = costoPorKmHabiles,
                                        onValueChange = { costoPorKmHabiles = it },
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
                                        onValueChange = { costoPorKmFeriados = it },
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
                                    ProfileField(
                                        label = "Kilómetros recorridos diarios",
                                        value = userData.repartidor.kmRecorridosDiarios.toString(),
                                        enabled = false
                                    )
                                }
                                TextButton(onClick = { showPasswordChange = !showPasswordChange }) {
                                    Text(if (showPasswordChange) "Ocultar cambio de contraseña" else "Cambiar contraseña")
                                }
                                if (showPasswordChange) {
                                    OutlinedTextField(
                                        value = currentPassword,
                                        onValueChange = { currentPassword = it },
                                        label = { Text("Contraseña actual") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = PasswordVisualTransformation(),
                                        isError = fieldErrors["currentPassword"] != null,
                                        supportingText = {
                                            fieldErrors["currentPassword"]?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    )
                                    OutlinedTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it },
                                        label = { Text("Nueva contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = PasswordVisualTransformation(),
                                        isError = fieldErrors["newPassword"] != null,
                                        supportingText = {
                                            fieldErrors["newPassword"]?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    )
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        label = { Text("Confirmar contraseña") },
                                        modifier = Modifier.fillMaxWidth(),
                                        visualTransformation = PasswordVisualTransformation(),
                                        isError = fieldErrors["confirmPassword"] != null,
                                        supportingText = {
                                            fieldErrors["confirmPassword"]?.let {
                                                Text(it, color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    )
                                }
                                Button(
                                    onClick = {
                                        perfilViewModel.updateProfile(
                                            nombre = nombre,
                                            direccion = direccion,
                                            telefono = telefono,
                                            correo = correo,
                                            numeroTarjeta = numeroTarjeta.takeIf { it.isNotBlank() },
                                            costoPorKmHabiles = costoPorKmHabiles.toDoubleOrNull(),
                                            costoPorKmFeriados = costoPorKmFeriados.toDoubleOrNull(),
                                            currentPassword = currentPassword,
                                            newPassword = newPassword,
                                            confirmPassword = confirmPassword,
                                            userType = userData
                                        ) {
                                            isEditing = false
                                            showPasswordChange = false
                                            loginViewModel.loadUserData()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Guardar") }
                                TextButton(onClick = {
                                    isEditing = false
                                    showPasswordChange = false
                                }) {
                                    Text("Cancelar")
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfileField(label = "Cédula", value = cedulaState, enabled = false)
                                ProfileField(
                                    label = "Nombre",
                                    value = (userData as? UserType.ClienteUser)?.cliente?.nombre
                                        ?: (userData as? UserType.RepartidorUser)?.repartidor?.nombre
                                        ?: (userData as? UserType.RestauranteUser)?.restaurante?.nombre
                                        ?: ""
                                )
                                ProfileField(
                                    label = "Dirección",
                                    value = (userData as? UserType.ClienteUser)?.cliente?.direccion
                                        ?: (userData as? UserType.RepartidorUser)?.repartidor?.direccion
                                        ?: (userData as? UserType.RestauranteUser)?.restaurante?.direccion
                                        ?: ""
                                )
                                if (userData is UserType.RestauranteUser) {
                                    ProfileField(
                                        label = "Tipo de Comida",
                                        value = userData.restaurante.tipoComida
                                    )
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(userData.restaurante.combos) { combo ->
                                            ProfileField(
                                                label = "Combo ${combo.numero}",
                                                value = "${combo.nombre} - ₡${combo.precio}",
                                                enabled = false
                                            )
                                        }
                                    }
                                }
                                if (userData !is UserType.RestauranteUser) {
                                    ProfileField(
                                        label = "Teléfono",
                                        value = (userData as? UserType.ClienteUser)?.cliente?.telefono
                                            ?: (userData as? UserType.RepartidorUser)?.repartidor?.telefono
                                            ?: ""
                                    )
                                    ProfileField(
                                        label = "Correo",
                                        value = (userData as? UserType.ClienteUser)?.cliente?.correo
                                            ?: (userData as? UserType.RepartidorUser)?.repartidor?.correo
                                            ?: ""
                                    )
                                    ProfileField(
                                        label = "Número de Tarjeta",
                                        value = (userData as? UserType.ClienteUser)?.cliente?.numeroTarjeta
                                            ?: (userData as? UserType.RepartidorUser)?.repartidor?.numeroTarjeta
                                            ?: "No proporcionado"
                                    )
                                }
                                if (userData is UserType.RepartidorUser) {
                                    ProfileField(
                                        label = "Costo por km (Hábiles)",
                                        value = userData.repartidor.costoPorKmHabiles.toString()
                                    )
                                    ProfileField(
                                        label = "Costo por km (Feriados)",
                                        value = userData.repartidor.costoPorKmFeriados.toString()
                                    )
                                    ProfileField(
                                        label = "Kilómetros recorridos diarios",
                                        value = userData.repartidor.kmRecorridosDiarios.toString()
                                    )
                                    ProfileField(
                                        label = "Estado",
                                        value = userData.repartidor.estado
                                    )
                                    ProfileField(
                                        label = "Amonestaciones",
                                        value = userData.repartidor.amonestaciones.toString()
                                    )
                                    ProfileField(
                                        label = "Quejas",
                                        value = userData.repartidor.quejas.joinToString(", ")
                                    )
                                }
                                Button(
                                    onClick = { isEditing = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Editar") }
                            }
                        }
                    }
                }
            }
            errorMessage.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { "No disponible" },
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
