package com.example.cletaeatsapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.cletaeatsapp.viewmodel.RestauranteRegistroViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RestauranteRegistroScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RestauranteRegistroViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val userType by loginViewModel.userType.collectAsStateWithLifecycle()
    val cedulaJuridica by viewModel.cedulaJuridica.collectAsStateWithLifecycle()
    val nombre by viewModel.nombre.collectAsStateWithLifecycle()
    val direccion by viewModel.direccion.collectAsStateWithLifecycle()
    val tipoComida by viewModel.tipoComida.collectAsStateWithLifecycle()
    val contrasena by viewModel.contrasena.collectAsStateWithLifecycle()
    val combos by viewModel.combos.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val fieldErrors by viewModel.fieldErrors.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val padding = if (isExpanded) 32.dp else 16.dp
    val scrollState = rememberScrollState()
    val showComboDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Restrict access to admins only
    LaunchedEffect(userType) {
        if (userType !is UserType.AdminUser) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (userType !is UserType.AdminUser) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registrar Restaurante",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "restaurant_form_transition"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cedulaJuridica,
                            onValueChange = viewModel::updateCedulaJuridica,
                            label = { Text("Cédula Jurídica") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["cedulaJuridica"] != null,
                            supportingText = {
                                fieldErrors["cedulaJuridica"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = viewModel::updateNombre,
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
                            onValueChange = viewModel::updateDireccion,
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
                            value = tipoComida,
                            onValueChange = viewModel::updateTipoComida,
                            label = { Text("Tipo de Comida") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["tipoComida"] != null,
                            supportingText = {
                                fieldErrors["tipoComida"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = viewModel::updateContrasena,
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fieldErrors["contrasena"] != null,
                            supportingText = {
                                fieldErrors["contrasena"]?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Text(
                            text = "Combos",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(combos) { combo ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.removeCombo(combo.numero) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${combo.numero}. ${combo.nombre} - ₡${combo.precio}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar combo"
                                        )
                                    }
                                }
                            }
                        }
                        if (fieldErrors["combos"] != null) {
                            Text(
                                text = fieldErrors["combos"]!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = { showComboDialog.value = true }, // Updated reference
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Agregar Combo")
                        }
                    }
                }
            }
            Button(
                onClick = {
                    viewModel.register { _, userType ->
                        if (userType is UserType.RestauranteUser) {
                            navController.navigate("restaurante_orders/${userType.restaurante.id}") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrar")
                }
            }
            errorMessage.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showComboDialog.value) { // Updated reference
        val comboNumero by viewModel.comboNumero.collectAsStateWithLifecycle()
        val comboNombre by viewModel.comboNombre.collectAsStateWithLifecycle()
        val comboPrecio by viewModel.comboPrecio.collectAsStateWithLifecycle()
        val comboFieldErrors by viewModel.comboFieldErrors.collectAsStateWithLifecycle()
        val isComboValid by viewModel.isComboValid.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = {
                showComboDialog.value = false // Updated reference
                viewModel.resetComboFields()
            },
            title = { Text("Agregar Combo", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = comboNumero,
                        onValueChange = viewModel::updateComboNumero,
                        label = { Text("Número (1-9)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = comboFieldErrors["comboNumero"] != null,
                        supportingText = {
                            comboFieldErrors["comboNumero"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = comboNombre,
                        onValueChange = viewModel::updateComboNombre,
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = comboFieldErrors["comboNombre"] != null,
                        supportingText = {
                            comboFieldErrors["comboNombre"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = comboPrecio,
                        onValueChange = viewModel::updateComboPrecio,
                        label = { Text("Precio (₡)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        isError = comboFieldErrors["comboPrecio"] != null,
                        supportingText = {
                            comboFieldErrors["comboPrecio"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addCombo()
                        showComboDialog.value = false // Updated reference
                    },
                    enabled = isComboValid
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showComboDialog.value = false // Updated reference
                    viewModel.resetComboFields()
                }) {
                    Text("Cancelar")
                }
            },
            modifier = Modifier.padding(horizontal = if (isExpanded) 24.dp else 16.dp)
        )
    }
}
