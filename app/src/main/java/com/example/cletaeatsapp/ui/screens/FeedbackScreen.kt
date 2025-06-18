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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.data.repository.UserType
import com.example.cletaeatsapp.ui.components.RequireRole
import com.example.cletaeatsapp.viewmodel.FeedbackViewModel
import com.example.cletaeatsapp.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun FeedbackScreen(
    pedido: Pedido,
    onFeedbackSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = hiltViewModel(),
    navController: NavController,
    loginViewModel: LoginViewModel
) {
    RequireRole(
        allowedRoles = setOf(UserType.ClientUser::class),
        navController = navController,
        loginViewModel = loginViewModel
    ) {
        val rating by viewModel.rating
        val comentario by viewModel.comentario
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val windowSizeClass =
            calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val padding = if (isExpanded) 32.dp else 16.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Calificar Repartidor") },
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
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Califique al repartidor",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "feedback_form_transition"
                ) { loading ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RatingBar(
                            rating = rating,
                            onRatingChanged = { viewModel.rating.intValue = it },
                            enabled = !loading
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { viewModel.comentario.value = it },
                            label = { Text("Comentario o queja") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            isError = errorMessage != null && comentario.isBlank()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.submitFeedback(pedido.repartidorId, onFeedbackSubmitted)
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
                        Text("Enviar")
                    }
                }
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(modifier = modifier) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Estrella $star",
                tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(enabled = enabled) { onRatingChanged(star) }
            )
        }
    }
}
