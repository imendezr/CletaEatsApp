package com.example.cletaeatsapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cletaeatsapp.viewmodel.LoginViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Perfil del Cliente",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cédula: ${viewModel.cedula.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Nombre: ${viewModel.nombre.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Dirección: ${viewModel.direccion.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Teléfono: ${viewModel.telefono.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Correo: ${viewModel.correo.value}")
    }
}
