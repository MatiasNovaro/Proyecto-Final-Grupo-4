package ar.ort.edu.proyecto_final_grupo_4.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.AddMedicationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard.HomeScreen
import android.content.Intent
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.editMedications.EditMedicationsScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.history.HistoryScreen

@Composable
fun Navigation(navController: NavHostController, onDestinationChanged: (String) -> Unit) {
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                Screens.Home.screen -> onDestinationChanged("Home")
                Screens.AddMedication.screen -> onDestinationChanged("Agregar Medicación")
                Screens.EditMedications.screen -> onDestinationChanged("")
                Screens.History.screen -> onDestinationChanged("Historial")

            }
        }
    }
    NavHost(navController = navController, startDestination = Screens.Home.screen) {
        composable(route = Screens.Home.screen) {
            HomeScreen(navController = navController)
        }
        composable(route = Screens.AddMedication.screen) {
            AddMedicationScreen(navController = navController)
        }
        composable("confirmMedication/{scheduleId}") { backStackEntry ->
            val scheduleId =
                backStackEntry.arguments?.getString("scheduleId")?.toLong() ?: return@composable
        }
            //ConfirmMedicationScreen(scheduleId = scheduleId)
            composable(route = Screens.EditMedications.screen) {
                EditMedicationsScreen(navController)
            }
            composable(route = Screens.History.screen) {
                HistoryScreen(navController = navController)
            }
        }
    }


