package ar.ort.edu.proyecto_final_grupo_4.ui.screens.editMedications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.MedicationViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.components.EditMedicationsItem
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.UserViewModel

@Composable
fun EditMedicationsScreen(
    navController: NavController,
) {
    val medicationViewModel: MedicationViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    val user by userViewModel.user.collectAsState()
    val medications by medicationViewModel.medications.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        userViewModel.ensureDefaultUser()
    }

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            medicationViewModel.loadMedications(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Editar Medicamentos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                        navController.navigate("editMedication/${medication.medicationID}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
