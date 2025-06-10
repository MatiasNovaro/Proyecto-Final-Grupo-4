package ar.ort.edu.proyecto_final_grupo_4.ui.screens.editMedications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.navigation.Screens
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.MedicationViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.components.EditMedicationsItem
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleViewModel
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.UserViewModel

@Composable
fun EditMedicationsScreen(
    navController: NavController,
) {
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()
    val medicationViewModel: MedicationViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    val user by userViewModel.user.collectAsState()
    val schedules by scheduleViewModel.schedules.collectAsState(initial = emptyList())
    val medications by medicationViewModel.medications.collectAsState(initial = emptyList())

    val showErrorDialog by medicationViewModel.showErrorDialog.collectAsState()

    LaunchedEffect(user) {
        if (user == null) {
            userViewModel.ensureDefaultUser()
        } else {
            user?.userID?.let { userId ->
                scheduleViewModel.loadSchedules()
                medicationViewModel.loadMedications(userId)
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { medicationViewModel.dismissErrorDialog() },
            title = { Text("No se puede eliminar") },
            text = { Text("No se puede eliminar este medicamento porque tiene registros de tomas asociados.") },
            confirmButton = {
                TextButton(onClick = { medicationViewModel.dismissErrorDialog() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn {
            items(
                items = medications.distinctBy { it.medicationID },
                key = { medication -> medication.medicationID }
            ) { medication ->
                EditMedicationsItem(
                    medication = medication,
                    onDelete = {
                        medicationViewModel.deleteMedication(medication)
                    },
                    onUpdate = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("medication", medication)
                        navController.navigate(Screens.UpdateMedication.screen)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}