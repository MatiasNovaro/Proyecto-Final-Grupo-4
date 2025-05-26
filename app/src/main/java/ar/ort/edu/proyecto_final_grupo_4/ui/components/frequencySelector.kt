package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyOption
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.getCommonFrequencies
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun FrequencySelector(
    selectedFrequency: FrequencyOption?,
    onFrequencySelected: (FrequencyOption) -> Unit,
    selectedWeekDays: List<Int>,
    onWeekDaysSelected: (List<Int>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    val commonFrequencies = remember { getCommonFrequencies() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Dropdown principal
        Box {
            OutlinedTextField(
                value = selectedFrequency?.displayName ?: "",
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
                commonFrequencies.forEach { frequency ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(frequency.displayName)
                                Text(
                                    frequency.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onFrequencySelected(frequency)
                            expanded = false
                        }
                    )
                }

                Divider()
                DropdownMenuItem(
                    text = { Text("Personalizar frecuencia...") },
                    onClick = {
                        expanded = false
                        showCustomDialog = true
                    }
                )
            }
        }

        // Selector de días para frecuencias semanales
        if (selectedFrequency?.frequencyType == FrequencyType.WEEKLY) {
            WeekDaysSelector(
                selectedDays = selectedWeekDays,
                onDaysSelected = onWeekDaysSelected
            )
        }

        // Descripción de la frecuencia seleccionada
        selectedFrequency?.let { frequency ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = frequency.description,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showCustomDialog) {
        CustomFrequencyDialog(
            onDismiss = { showCustomDialog = false },
            onFrequencyCreated = { customFrequency ->
                onFrequencySelected(customFrequency)
                showCustomDialog = false
            }
        )
    }
}

@Composable
private fun WeekDaysSelector(
    selectedDays: List<Int>,
    onDaysSelected: (List<Int>) -> Unit
) {
    val dayNames = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

    Column {
        Text(
            "Días de la semana:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(dayNames) { index, dayName ->
                FilterChip(
                    onClick = {
                        val newSelection = if (selectedDays.contains(index)) {
                            selectedDays - index
                        } else {
                            selectedDays + index
                        }
                        onDaysSelected(newSelection)
                    },
                    label = { Text(dayName) },
                    selected = selectedDays.contains(index)
                )
            }
        }
    }
}

@Composable
private fun CustomFrequencyDialog(
    onDismiss: () -> Unit,
    onFrequencyCreated: (FrequencyOption) -> Unit
) {
    var selectedType by remember { mutableStateOf(FrequencyType.HOURS_INTERVAL) }
    var intervalValue by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val interval = intervalValue.toIntOrNull()
                    if (displayName.isNotBlank() && (interval != null || selectedType == FrequencyType.AS_NEEDED)) {
                        val customFrequency = FrequencyOption(
                            id = "custom_${System.currentTimeMillis()}",
                            displayName = displayName,
                            frequencyType = selectedType,
                            intervalValue = interval,
                            description = "Frecuencia personalizada"
                        )
                        onFrequencyCreated(customFrequency)
                    }
                },
                enabled = displayName.isNotBlank() &&
                        (intervalValue.toIntOrNull() != null || selectedType == FrequencyType.AS_NEEDED)
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Frecuencia Personalizada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej: Cada 5 horas") }
                )

                // Selector de tipo
                var expandedType by remember { mutableStateOf(false) }
                val typeOptions = listOf(
                    FrequencyType.HOURS_INTERVAL to "Cada X horas",
                    FrequencyType.DAYS_INTERVAL to "Cada X días",
                    FrequencyType.TIMES_PER_DAY to "X veces al día"
                )

                Box {
                    OutlinedTextField(
                        value = typeOptions.find { it.first == selectedType }?.second ?: "",
                        onValueChange = {},
                        label = { Text("Tipo") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedType = true }
                    )

                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        typeOptions.forEach { (type, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedType = type
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // Campo de intervalo
                if (selectedType != FrequencyType.AS_NEEDED) {
                    OutlinedTextField(
                        value = intervalValue,
                        onValueChange = { intervalValue = it },
                        label = { Text("Intervalo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Ej: 6") }
                    )
                }
            }
        }
    )
}