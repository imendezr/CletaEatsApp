package com.example.cletaeatsapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cletaeatsapp.data.model.UserType
import com.example.cletaeatsapp.viewmodel.LoginViewModel
import kotlin.reflect.KClass

@Composable
fun RequireRole(
    allowedRoles: Set<KClass<out UserType>>,
    navController: NavController,
    loginViewModel: LoginViewModel,
    content: @Composable () -> Unit
) {
    val userType by loginViewModel.userType.collectAsStateWithLifecycle()
    val isAuthorized = remember(userType) {
        userType?.let { allowedRoles.any { role -> it::class == role } } == true
    }

    LaunchedEffect(userType) {
        if (!isAuthorized) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (isAuthorized) {
        content()
    }
}
