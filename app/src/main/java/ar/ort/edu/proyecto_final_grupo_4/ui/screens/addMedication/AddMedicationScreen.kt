package ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication

import TimeInputStyled
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.MedicationViewModel
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.UserViewModel
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyOption
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.components.FrequencySelector
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomTopBar
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import java.time.LocalTime
import java.util.Calendar
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(navController: NavController) {
    val userVM: UserViewModel = hiltViewModel()
    val medVM: MedicationViewModel = hiltViewModel()
    val scheduleVM: ScheduleViewModel = hiltViewModel()

    val user by userVM.user.collectAsState()
    val units by medVM.dosageUnits.collectAsState()

    var name by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("Diariamente") }
    var selectedFrequency by remember { mutableStateOf<FrequencyOption?>(null) }
    var selectedWeekDays by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedUnit by remember { mutableStateOf<DosageUnit?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userVM.ensureDefaultUser()
        medVM.loadDosageUnits()
    }
    LaunchedEffect(name) {
        if (name.isNotBlank()) {
            medVM.checkMedicationNameExists(name) { exists ->
                nameError = if (exists) "Este medicamento ya existe" else null
            }
        } else {
            nameError = null
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MedicationBasicFields(
                    name = name,
                    onNameChange = { name = it },
                    dosis = dosis,
                    onDosisChange = { dosis = it },
                    nameError = nameError
                )

                FrequencySelector(
                    selectedFrequency = selectedFrequency,
                    onFrequencySelected = { selectedFrequency = it },
                    selectedWeekDays = selectedWeekDays,
                    onWeekDaysSelected = { selectedWeekDays = it }
                )

                DosageUnitDropdown(
                    units = units,
                    selectedUnit = selectedUnit,
                    onUnitSelected = { selectedUnit = it },
                    onUnitAdded = { newUnit ->
                        medVM.addDosageUnit(newUnit) { savedUnit ->
                            selectedUnit = savedUnit
                        }
                    }
                )

                TimeSelector(
                    selectedTime = selectedTime,
                    onTimeSelected = { selectedTime = it }
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            SaveButton(
                enabled = name.isNotBlank() && nameError == null &&
                        dosis.isNotBlank() && selectedUnit != null &&
                        selectedTime != null && selectedFrequency != null,
                onClick = {
                    user?.let { user ->
                        selectedUnit?.let { unit ->
                            selectedTime?.let { time ->
                                selectedFrequency?.let { frequency ->
                                    val med = Medication(
                                        medicationID = 0,
                                        userID = user.userID,
                                        name = name,
                                        dosage = dosis,
                                        dosageUnitID = unit.dosageUnitID
                                    )
                                    medVM.addMedicationWithScheduleAndFrequency(
                                        medication = med,
                                        frequency = frequency,
                                        startTime = time,
                                        selectedWeekDays = selectedWeekDays,
                                        scheduleVM = scheduleVM,
                                    )

                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("¡Guardado con éxito!")
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}



@Composable
private fun MedicationBasicFields(
    name: String,
    onNameChange: (String) -> Unit,
    dosis: String,
    onDosisChange: (String) -> Unit,
    nameError: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                placeholder = {
                    Text(
                        "Ej. Paracetamol",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                singleLine = true
            )

            if (nameError != null) {
                Text(
                    text = nameError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        OutlinedTextField(
            value = dosis,
            onValueChange = onDosisChange,
            label = { Text("Dosis") },
            placeholder = {
                Text(
                    "Ej. 500 mg",
                    color = Color.Gray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun FrequencyDropdown(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val frecuencias = listOf("Diariamente", "Cada 8 horas", "Semanalmente")

    Box {
        OutlinedTextField(
            value = selectedFrequency,
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
                        onFrequencySelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DosageUnitDropdown(
    units: List<DosageUnit>,
    selectedUnit: DosageUnit?,
    onUnitSelected: (DosageUnit) -> Unit,
    onUnitAdded: (DosageUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedUnit?.name ?: "",
            onValueChange = {},
            label = { Text("Unidad de Dosis") },
            placeholder = {
                Text(
                    "Seleccionar unidad",
                    color = Color.Gray
                )
            },
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
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }

            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Agregar nueva unidad...") },
                onClick = {
                    expanded = false
                    showAddUnitDialog = true
                }
            )
        }
    }

    if (showAddUnitDialog) {
        AddUnitDialog(
            onDismiss = { showAddUnitDialog = false },
            onUnitAdded = { unit ->
                onUnitAdded(unit)
                showAddUnitDialog = false
            }
        )
    }
}

@Composable
private fun AddUnitDialog(
    onDismiss: () -> Unit,
    onUnitAdded: (DosageUnit) -> Unit
) {
    var newUnitName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = newUnitName.trim()
                    if (trimmedName.isNotEmpty()) {
                        val newUnit = DosageUnit(dosageUnitID = 0, name = trimmedName)
                        onUnitAdded(newUnit)
                        newUnitName = ""
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                newUnitName = ""
            }) {
                Text("Cancelar")
            }
        },
        title = { Text("Nueva Unidad de Dosis") },
        text = {
            OutlinedTextField(
                value = newUnitName,
                onValueChange = { newUnitName = it },
                label = { Text("Nombre de la unidad") },
                placeholder = {
                    Text(
                        "Ej. mg, ml, pastillas",
                        color = Color.Gray
                    )
                },
                singleLine = true
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    OutlinedTextField(
        value = selectedTime?.let {
            String.format("%02d:%02d", it.hour, it.minute)
        } ?: "",
        onValueChange = {},
        label = { Text("Horario de Toma") },
        placeholder = {
            Text(
                "Seleccionar hora",
                color = Color.Gray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Seleccionar hora",
                modifier = Modifier.clickable { showTimePicker = true }
            )
        }
    )

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            TimeInputStyled(
                timePickerState = timePickerState,
                onConfirm = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    onTimeSelected(LocalTime.of(hour, minute))
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryOrange
        )
    ) {
        Text(
            text = "Guardar",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun AddMedicationScreenPreview() {
//    MaterialTheme {
//        AddMedicationScreen()
//    }
//}