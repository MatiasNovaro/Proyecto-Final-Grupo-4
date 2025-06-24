package ar.ort.edu.proyecto_final_grupo_4.ui.screens.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
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
import ar.ort.edu.proyecto_final_grupo_4.domain.model.HistoryYUiItem
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.MedicationLogViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.LightCream
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneOffset
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomTopBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val medicationLogViewModel: MedicationLogViewModel = hiltViewModel()
    val historyLogs by medicationLogViewModel.historyLogs.collectAsState()

    var showFilterOptions by remember { mutableStateOf(false) }
    var selectedFilterStatus by remember { mutableStateOf<Boolean?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = null
    )
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopBar(
                navController = navController,
                actions = {
                    IconButton(onClick = { showFilterOptions = !showFilterOptions }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = Color.Black
                        )
                    }
                },
                backgroundColor = LightCream
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightCream)
                .padding(horizontal = 16.dp)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (showFilterOptions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilterStatus == null,
                        onClick = {
                            selectedFilterStatus = null
                            medicationLogViewModel.setFilterByTakenStatus(null)
                        },
                        label = { Text("Todos") },
                        leadingIcon = if (selectedFilterStatus == null) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null
                    )
                    FilterChip(
                        selected = selectedFilterStatus == true,
                        onClick = {
                            selectedFilterStatus = true
                            medicationLogViewModel.setFilterByTakenStatus(true)
                        },
                        label = { Text("Tomados") },
                        leadingIcon = if (selectedFilterStatus == true) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null
                    )
                    FilterChip(
                        selected = selectedFilterStatus == false,
                        onClick = {
                            selectedFilterStatus = false
                            medicationLogViewModel.setFilterByTakenStatus(false)
                        },
                        label = { Text("Omitidos") },
                        leadingIcon = if (selectedFilterStatus == false) {
                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                        } else null
                    )

                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar Fecha")
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = datePickerState.selectedDateMillis?.let {
                                LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC)
                                    .toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            } ?: "Seleccionar Fecha",
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (historyLogs.isEmpty()) {
                Text("No hay registros de medicamentos aún.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val groupedLogs = historyLogs.groupBy { it.dayLabel }

                    items(groupedLogs.keys.toList()) { dayLabel ->
                        val logsForDay = groupedLogs[dayLabel] ?: emptyList()
                        HistoryDaySection(
                            dayLabel = dayLabel,
                            logs = logsForDay
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val selectedLocalDate = LocalDateTime.ofEpochSecond(selectedMillis / 1000, 0, ZoneOffset.UTC).toLocalDate()
                        medicationLogViewModel.setFilterByDateRange(
                            selectedLocalDate.atStartOfDay(),
                            selectedLocalDate.atTime(23, 59, 59, 999999999)
                        )
                    } else {
                        medicationLogViewModel.setFilterByDateRange(null, null)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun HistoryDaySection(
    dayLabel: String,
    logs: List<HistoryYUiItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dayLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            logs.forEach { log ->
                HistoryMedicationLogItem(log = log)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HistoryMedicationLogItem(log: HistoryYUiItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Hora",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = log.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.width(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = log.medicationName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = log.dosage,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Icon(
            imageVector = if (log.wasTaken) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = if (log.wasTaken) "Tomado" else "Omitido",
            tint = if (log.wasTaken) Color.Green else PrimaryOrange,
            modifier = Modifier.size(24.dp)
        )
    }
}
