package com.example.cletaeatsapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cletaeatsapp.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val cedula by viewModel.cedula
    val nombre by viewModel.nombre
    val direccion by viewModel.direccion
    val telefono by viewModel.telefono
    val correo by viewModel.correo
    val errorMessage by viewModel.errorMessage
    val isRegistering by viewModel.isRegistering
    val isLoading by viewModel.isLoading
    Log.d(
        "LoginScreen",
        "Recomposing LoginScreen, isLoading: $isLoading, isRegistering: $isRegistering"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Registro" else "Iniciar Sesión",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = cedula,
            onValueChange = { viewModel.cedula.value = it },
            label = { Text("Cédula") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        if (isRegistering) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { viewModel.nombre.value = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = direccion,
                onValueChange = { viewModel.direccion.value = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = telefono,
                onValueChange = { viewModel.telefono.value = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = correo,
                onValueChange = { viewModel.correo.value = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (isRegistering) {
                    Log.d("LoginScreen", "Register button clicked")
                    viewModel.register { cedula ->
                        Log.d("LoginScreen", "Register success, navigating with cedula: $cedula")
                        onLoginSuccess(cedula)
                    }
                } else {
                    Log.d("LoginScreen", "Login button clicked")
                    viewModel.login { cedula ->
                        Log.d("LoginScreen", "Login success, navigating with cedula: $cedula")
                        onLoginSuccess(cedula)
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
                Text(if (isRegistering) "Registrar" else "Iniciar Sesión")
            }
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
