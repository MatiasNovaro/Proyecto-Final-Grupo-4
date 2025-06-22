package ar.ort.edu.proyecto_final_grupo_4.ui.screens.editMedications

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.presentation.viewmodel.MedicationDetailEvent
import ar.ort.edu.proyecto_final_grupo_4.presentation.viewmodel.MedicationDetailViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomButton
import ar.ort.edu.proyecto_final_grupo_4.ui.components.FrequencySelector
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.TimeSelector
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.LightCream
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction2

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen( // Renamed
    navController: NavController,
    medicationId: Long // Now takes medicationId
) {
    val viewModel: MedicationDetailViewModel = hiltViewModel() // Use new ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.eventFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(medicationId) {
        viewModel.loadMedicationDetails(medicationId)
    }

    LaunchedEffect(event) {
        event?.let { detailEvent ->
            when (detailEvent) {
                MedicationDetailEvent.SaveSuccess -> {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Medicamento actualizado con éxito!") }
                    navController.popBackStack()
                }
                is MedicationDetailEvent.SaveFailure -> {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Error: ${detailEvent.message}") }
                }

                else -> {}
            }
            viewModel.clearEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Medicamento",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightCream
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.errorMessage ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightCream)
                    .padding(horizontal = 16.dp)
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Medication Name
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Nombre del Medicamento") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dosage
                OutlinedTextField(
                    value = uiState.dosage,
                    onValueChange = viewModel::updateDosage,
                    label = { Text("Dosis") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Dosage Unit Dropdown (reusing your component)
                DosageUnitEditDropdown(
                    units = uiState.availableDosageUnits,
                    selectedUnit = uiState.selectedUnit,
                    onUnitSelected = viewModel::updateSelectedUnit,
                    onAddUnitRequest = viewModel::addDosageUnit
                )

                // Frequency Selector (editable now, reusing your component)
                FrequencySelector(
                    selectedFrequency = uiState.selectedFrequency,
                    onFrequencySelected = viewModel::updateSelectedFrequency,
                    selectedWeekDays = uiState.selectedWeekDays,
                    onWeekDaysSelected = viewModel::updateSelectedWeekDays
                )

                // Schedule Time Selector (reusing your component)
                TimeSelector(
                    selectedTime = uiState.startTime,
                    onTimeSelected = viewModel::updateStartTime
                )

                // Active/Inactive Toggle for the entire medication's schedule
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activar/Desactivar Horario", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.isActive,
                        onCheckedChange = viewModel::updateIsActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryOrange,
                            checkedTrackColor = PrimaryOrange.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomButton(
                    text = "Guardar Cambios",
                    modifier = Modifier.fillMaxWidth(),
                    buttonColor = PrimaryOrange,
                    onClick = viewModel::saveChanges,
                )
            }
        }
    }
}


@Composable
fun DosageUnitEditDropdown(
    units: List<DosageUnit>,
    selectedUnit: DosageUnit?,
    onUnitSelected: (DosageUnit) -> Unit,
    onAddUnitRequest: (String, (DosageUnit) -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedUnit?.name ?: "",
            onValueChange = {},
            label = { Text("Unidad de Dosis") },
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
            onUnitAdded = { newDosageUnit ->
                onAddUnitRequest(newDosageUnit.name) { savedUnit ->
                    onUnitSelected(savedUnit)
                    showAddUnitDialog = false
                }
            }
        )
    }
}
@Composable
fun AddUnitDialog(
    onDismiss: () -> Unit,
    onUnitAdded: (DosageUnit) -> Unit
) {
    var unitName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Agregar nueva unidad") },
        text = {
            TextField(
                value = unitName,
                onValueChange = { unitName = it },
                label = { Text("Nombre de la unidad") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (unitName.isNotBlank()) {
                        onUnitAdded(DosageUnit(name = unitName))
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}