package ar.ort.edu.proyecto_final_grupo_4.navigation

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.MedicationConfirmationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.AddMedicationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard.HomeScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.login.LoginScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.login.RegisterScreen
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.AuthViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.editMedications.EditMedicationsScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.history.HistoryScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.reminder.ReminderScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable 
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.editMedications.MedicationDetailScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.login.CambiarContraseniaScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.login.CambiarNombreScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.settings.AjustesScreen

@Composable

fun Navigation(navController: NavHostController, onDestinationChanged: (String) -> Unit, authViewModel: AuthViewModel) {
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
    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isUserLoggedIn()) Screens.Home.screen else Screens.LoginScreen.screen
    ) {
        composable(route = Screens.Home.screen) {
            HomeScreen(navController = navController)
        }
        composable(route = Screens.AddMedication.screen) {
            AddMedicationScreen(navController = navController)
        }
        composable(route = Screens.LoginScreen.screen) {
            LoginScreen(navController = navController)
        }
        composable(route = Screens.RegisterScreen.screen) {
            RegisterScreen(navController = navController)

        }
        composable(route = Screens.EditMedications.screen) {
            EditMedicationsScreen(navController)
        }
        composable(route = Screens.History.screen) {
            HistoryScreen(navController = navController)
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

        composable(route = Screens.AjustesScreen.screen) {
            AjustesScreen(navController = navController)
        }

        composable(route = Screens.CambiarNombreScreen.screen) {
            CambiarNombreScreen(navController = navController)
        }

        composable(route = Screens.CambiarContraseniaScreen.screen) {
            CambiarContraseniaScreen(navController = navController)
        }

        composable(route = "medication_confirmation/{scheduleIds}/{fromNotification}",
            arguments = listOf(
                navArgument("scheduleIds") { type = NavType.StringType },
                navArgument("fromNotification") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val scheduleIdsString = backStackEntry.arguments?.getString("scheduleIds") ?: ""
            val isFromNotification = backStackEntry.arguments?.getBoolean("fromNotification") ?: false
            val scheduleIdsList = scheduleIdsString.split(",").mapNotNull { it.toLongOrNull() }

            MedicationConfirmationScreen(
                scheduleIds = scheduleIdsList,
                fromNotification = isFromNotification,
                navController = navController,
            )
        }
        composable(
            route = "medication_details/{medicationId}", // Define the route with argument
            arguments = listOf(
                navArgument("medicationId") { type = NavType.LongType } // Specify type
            )
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId")
            if (medicationId != null) {
                MedicationDetailScreen(navController = navController, medicationId = medicationId) // Use new screen
            } else {
                Log.e("Navigation", "MedicationDetailScreen: Missing medicationId argument")
                navController.popBackStack()
            }
        }
    }

    }


