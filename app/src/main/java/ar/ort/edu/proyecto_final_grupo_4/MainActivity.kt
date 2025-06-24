package ar.ort.edu.proyecto_final_grupo_4

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ar.ort.edu.proyecto_final_grupo_4.navigation.Navigation
import ar.ort.edu.proyecto_final_grupo_4.ui.components.BottomNavigationBar
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomTopBar
import androidx.fragment.app.FragmentActivity
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.ProyectoFinalGrupo4Theme
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoFinalGrupo4Theme {

                val authViewModel: AuthViewModel = viewModel()
                val navController = rememberNavController()
                val currentTitle = remember { mutableStateOf("Home") }

                // Observar cambios de navegación
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // --- START CHANGES HERE ---
                // Extract arguments from the intent that launched the activity
                val navigateToRoute = intent?.getStringExtra("navigate_to")
                val scheduleIdsArray = intent?.getLongArrayExtra("scheduleIds")
                val fromNotification = intent?.getBooleanExtra("fromNotification", false) ?: false

                // Use LaunchedEffect to navigate AFTER the NavController is ready
                LaunchedEffect(Unit) { // Use Unit as key to run once
                    if (navigateToRoute == "medication_confirmation" && scheduleIdsArray != null) {
                        val scheduleIdsString = scheduleIdsArray.joinToString(separator = ",")
                        navController.navigate("medication_confirmation/$scheduleIdsString/$fromNotification") {
                            // Clear back stack to prevent going back to home after confirmation
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    } else if (authViewModel.isUserLoggedIn()) {
                        // Regular startup: navigate to home if logged in
                        navController.navigate(ar.ort.edu.proyecto_final_grupo_4.navigation.Screens.Home.screen) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    } else {
                        // Regular startup: navigate to login if not logged in
                        navController.navigate(ar.ort.edu.proyecto_final_grupo_4.navigation.Screens.LoginScreen.screen) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
                // --- END CHANGES HERE ---

                // Definir en qué pantallas NO mostrar la bottom bar
                val hideBottomBarRoutes = listOf(
                    "LoginScreen",
                    "RegisterScreen",
                    "BiometricLogin"
                )

                val shouldShowBottomBar = currentRoute != null &&
                        !hideBottomBarRoutes.any { route -> currentRoute.startsWith(route) }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CustomTopBar(
                            navController = navController,
                            title = currentTitle.value,
                            modifier = Modifier.height(30.dp)
                        )
                    },
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            BottomNavigationBar(navController = navController, authViewModel = authViewModel)
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Pass a default start destination, as initial navigation is handled by LaunchedEffect above
                        Navigation(
                            navController = navController,
                            onDestinationChanged = { title -> currentTitle.value = title },
                            authViewModel = authViewModel,
                            // Start destination handled by LaunchedEffect
                        )
                    }
                }
            }
        }
    }
}


