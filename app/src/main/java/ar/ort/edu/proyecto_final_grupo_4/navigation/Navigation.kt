package ar.ort.edu.proyecto_final_grupo_4.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.AddMedicationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard.HomeScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.login.LoginScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.login.RegisterScreen
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.AuthViewModel

@Composable
fun Navigation(navController: NavHostController, onDestinationChanged: (String) -> Unit, authViewModel: AuthViewModel){
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                Screens.Home.screen -> onDestinationChanged("Home")
                Screens.AddMedication.screen -> onDestinationChanged("Agregar Medicación")
            }
        }
    }
    NavHost(navController = navController,
        startDestination = if (authViewModel.isUserLoggedIn()) Screens.Home.screen else Screens.LoginScreen.screen){
        composable(route= Screens.Home.screen){
            HomeScreen(navController= navController )
        }
        composable(route= Screens.AddMedication.screen){
            AddMedicationScreen(navController= navController )
        }
        composable(route= Screens.LoginScreen.screen){
            LoginScreen(navController= navController )
        }
        composable(route= Screens.RegisterScreen.screen){
            RegisterScreen(navController= navController )
        }
    }
}