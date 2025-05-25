package ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication

import TimeInputStyled
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.MedicationViewModel
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.UserViewModel
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import java.util.Calendar

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen() {
    val userVM: UserViewModel = hiltViewModel()
    val medVM: MedicationViewModel = hiltViewModel()

    val user by userVM.user.collectAsState()

    var name by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("Diariamente") }
    var expanded by remember { mutableStateOf(false) }
    val frecuencias = listOf("Diariamente", "Cada 8 horas", "Semanalmente")
    var selectedUnit by remember { mutableStateOf<DosageUnit?>(null) }
    var expandedUnits by remember { mutableStateOf(false) }
    val units by medVM.dosageUnits.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var hourSelected by remember { mutableStateOf<Int?>(null) }
    var minuteSelected by remember { mutableStateOf<Int?>(null) }

    // Create timePickerState at the screen level
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    LaunchedEffect(Unit) {
        userVM.ensureDefaultUser()
        medVM.loadDosageUnits()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Agregar",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Medicamento",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            placeholder = { Text("Ej. Paracetamol") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dosis,
            onValueChange = { dosis = it },
            label = { Text("Dosis") },
            placeholder = { Text("Ej. 500 mg") },
            modifier = Modifier.fillMaxWidth()
        )

        // Frequency dropdown
        Box {
            OutlinedTextField(
                value = frecuencia,
                onValueChange = {},
                label = { Text("Frecuencia") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        modifier = Modifier.clickable { expanded = true }
                    )
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                frecuencias.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            frecuencia = option
                            expanded = false
                        }
                    )
                }
            }
        }

        // Dosage unit dropdown
        Box {
            OutlinedTextField(
                value = selectedUnit?.name ?: "",
                onValueChange = {},
                label = { Text("Unidad de Dosis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedUnits = true },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        modifier = Modifier.clickable { expandedUnits = true }
                    )
                }
            )

            DropdownMenu(
                expanded = expandedUnits,
                onDismissRequest = { expandedUnits = false }
            ) {
                units.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            selectedUnit = unit
                            expandedUnits = false
                        }
                    )
                }
            }
        }

        // Time selection button
        Button(onClick = { showTimePicker = true }) {
            Text(
                if (hourSelected != null && minuteSelected != null) {
                    "Hora seleccionada: ${String.format("%02d:%02d", hourSelected, minuteSelected)}"
                } else {
                    "Seleccionar hora de toma"
                }
            )
        }

        // Time picker dialog
        if (showTimePicker) {
            Dialog(onDismissRequest = { showTimePicker = false }) {
                TimeInputStyled(
                    timePickerState = timePickerState,
                    onConfirm = {
                        hourSelected = timePickerState.hour
                        minuteSelected = timePickerState.minute
                        showTimePicker = false
                    },
                    onDismiss = { showTimePicker = false }
                )
            }
        }

        // Save button
        Button(
            onClick = {
                user?.let {
                    selectedUnit?.let { unit ->
                        val med = Medication(
                            medicationID = 0,
                            userID = it.userID,
                            name = name,
                            dosage = dosis,
                            dosageUnitID = unit.dosageUnitID
                        )
                        medVM.addMedication(med)
                    }
                }
            },
            enabled = name.isNotBlank() && dosis.isNotBlank() && selectedUnit != null
        ) {
            Text("Guardar")
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMedicationScreenPreview() {
    MaterialTheme {
        AddMedicationScreen()
    }
}
