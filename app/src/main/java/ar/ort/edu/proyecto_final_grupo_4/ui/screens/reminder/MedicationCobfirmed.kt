package ar.ort.edu.proyecto_final_grupo_4.ui.screens // Adjust package as needed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
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
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationStatus
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduleWithMedication
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.MedicationViewModel
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ReminderViewModel
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleViewModel
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MedicationConfirmationScreen(
    scheduleIds: List<Long>,
    fromNotification: Boolean,
    navController: NavController,
) {
    val viewModel: ScheduleViewModel = hiltViewModel()
    val scheduledMedications by viewModel.getSchedulesWithMedications(scheduleIds).collectAsState(initial = emptyList())

    val isLoading = scheduledMedications.isEmpty() && scheduleIds.isNotEmpty()
    var showSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💊 Confirmación de medicamentos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (fromNotification) {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(Icons.Default.Block, contentDescription = "Cerrar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current time
            Text(
                text = "Hora actual: ${getCurrentTimeString()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Medication list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Now iterating over ScheduleWithMedication objects
                items(scheduledMedications) { scheduleWithMedication ->
                    MedicationConfirmationCard(
                        scheduleWithMedication = scheduleWithMedication, // Pass the combined object
                        onTaken = {
                            viewModel.markAsTaken(scheduleWithMedication.schedule.scheduleID)
                        },
                        onSkipped = {
                            viewModel.markAsSkipped(scheduleWithMedication.schedule.scheduleID)
                        },
                        onSnooze = { minutes ->
                            viewModel.snoozeAlarm(scheduleWithMedication.schedule.scheduleID, minutes)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                OutlinedButton(
//                    onClick = {
//                        scheduledMedications.forEach { scheduleWithMedication ->
//                            viewModel.snoozeAlarm(scheduleWithMedication.schedule.scheduleID, 5)
//                        }
//                        showSuccessMessage = true
//                        navController.navigateUp()
//                    },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Icon(Icons.Default.Schedule, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Posponer todo")
//                }
//
//                Button(
//                    onClick = {
//                        scheduledMedications.forEach { scheduleWithMedication ->
//                            viewModel.markAsTaken(scheduleWithMedication.schedule.scheduleID)
//                        }
//                        showSuccessMessage = true
//                        navController.navigateUp()
//                    },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Icon(Icons.Default.Check, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Todos tomados")
//                }
//            }
        }
    }

    // Success message feedback
    if (showSuccessMessage) {
        LaunchedEffect(showSuccessMessage) {
            delay(2000) // Show message for 2 seconds
            showSuccessMessage = false
        }
    }
}

@Composable
fun MedicationConfirmationCard(
    scheduleWithMedication: ScheduleWithMedication,
    onTaken: () -> Unit,
    onSkipped: () -> Unit,
    onSnooze: (Int) -> Unit,
) {
    val reminderViewModel: ReminderViewModel = hiltViewModel()

    // Load the dosage unit when the scheduleWithMedication changes
    LaunchedEffect(scheduleWithMedication) {
        reminderViewModel.getDosageUnit(scheduleWithMedication.medication.dosageUnitID)
    }

    // Collect the dosage unit state
    val dosageUnit by reminderViewModel.dosageUnit.collectAsState() // This holds DosageUnit?

    var isExpanded by remember { mutableStateOf(false) }
    var showSnoozeOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Medication header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scheduleWithMedication.medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${scheduleWithMedication.medication.dosage} ${dosageUnit?.name.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Programado: ${scheduleWithMedication.schedule.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status indicator (no changes here)
                when (scheduleWithMedication.schedule.status) {
                    MedicationStatus.PENDING -> {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Pendiente",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    MedicationStatus.TAKEN -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Tomado",
                            tint = Color.Green
                        )
                    }
                    MedicationStatus.SKIPPED -> {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Omitido",
                            tint = Color.Red
                        )
                    }
                }
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                if (scheduleWithMedication.medication.name.isNotEmpty()) {
                    Text(
                        text = "Dosis:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${scheduleWithMedication.medication.dosage} ${dosageUnit?.name.orEmpty()}", // Combine dosage with unit name here too
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action buttons (no changes here)
                if (scheduleWithMedication.schedule.status == MedicationStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSnoozeOptions = !showSnoozeOptions },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Posponer", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = onSkipped,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Omitir", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onTaken,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tomado", fontSize = 11.sp)
                        }
                    }

                    // Snooze options (no changes here)
                    if (showSnoozeOptions) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30).forEach { minutes ->
                                OutlinedButton(
                                    onClick = {
                                        onSnooze(minutes)
                                        showSnoozeOptions = false
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${minutes}m")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getCurrentTimeString(): String {
    return remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}