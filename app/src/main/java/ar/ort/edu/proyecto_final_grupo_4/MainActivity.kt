package ar.ort.edu.proyecto_final_grupo_4

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.Screens
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.AddMedicationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.biometricLogin.BiometricLoginScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard.HomeScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.ProyectoFinalGrupo4Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoFinalGrupo4Theme {
                MainNavigation()
            }
        }
    }
}

@Composable
private fun MainNavigation() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = Screens.BiometricLogin.screen
        ) {
            composable(Screens.BiometricLogin.screen) {
                BiometricLoginScreen(
                    onAuthenticationSuccess = {
                        navController.navigate(Screens.Home.screen) {
                            popUpTo(Screens.BiometricLogin.screen) {
                                inclusive = true
                            }
                        }
                    },
                    onAuthenticationError = { error ->
                        // Manejar errores si es necesario
                    }
                )
            }

            composable(Screens.Home.screen) {
                HomeScreen(navController)
            }

            composable(Screens.AddMedication.screen) {
                AddMedicationScreen()
            }
        }
    }
}