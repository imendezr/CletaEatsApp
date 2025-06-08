package com.example.cletaeatsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cletaeatsapp.ui.screens.LoginScreen
import com.example.cletaeatsapp.ui.screens.OrdersScreen
import com.example.cletaeatsapp.ui.screens.ProfileScreen
import com.example.cletaeatsapp.ui.screens.RestaurantListScreen
import com.example.cletaeatsapp.ui.theme.CletaEatsAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CletaEatsAppTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Column {
                            Text(
                                text = "Perfil",
                                modifier = Modifier
                                    .testTag("drawer_profile")
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("profile")
                                    }
                                    .padding(16.dp)
                            )
                            Text(
                                text = "Restaurantes",
                                modifier = Modifier
                                    .testTag("drawer_restaurants")
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("restaurants")
                                    }
                                    .padding(16.dp)
                            )
                            Text(
                                text = "Pedidos",
                                modifier = Modifier
                                    .testTag("drawer_orders")
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("orders")
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                ) {
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { navController.navigate("restaurants") }
                            )
                        }
                        composable("restaurants") {
                            RestaurantListScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable("profile") {
                            ProfileScreen()
                        }
                        composable("orders") {
                            OrdersScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                }
            }
        }
    }
}
