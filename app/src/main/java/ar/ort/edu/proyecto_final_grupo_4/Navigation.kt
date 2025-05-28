package ar.ort.edu.proyecto_final_grupo_4

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.Screens
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.AddMedicationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard.HomeScreen

@Composable
fun Navigation(navController: NavHostController, onDestinationChanged: (String) -> Unit){
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                Screens.Home.screen -> onDestinationChanged("Home")
                Screens.AddMedication.screen -> onDestinationChanged("Agregar Medicación")
            }
        }
    }
    NavHost(navController = navController, startDestination = Screens.Home.screen){
        composable(route=Screens.Home.screen){
            HomeScreen(navController= navController )
        }
        composable(route=Screens.AddMedication.screen){
            AddMedicationScreen(navController= navController )
        }
    }
}