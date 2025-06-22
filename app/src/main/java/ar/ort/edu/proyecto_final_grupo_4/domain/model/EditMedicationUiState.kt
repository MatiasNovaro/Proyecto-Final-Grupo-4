package ar.ort.edu.proyecto_final_grupo_4.domain.model

import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyOption
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import java.time.LocalTime

data class EditMedicationUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val medicationId: Long = 0,
    val name: String = "",
    val dosage: String = "",
    val selectedUnit: DosageUnit? = null,
    val availableDosageUnits: List<DosageUnit> = emptyList(),
    val selectedFrequency: FrequencyOption? = null,
    val selectedWeekDays: List<Int> = emptyList(),
    val startTime: LocalTime = LocalTime.now(),
    val isActive: Boolean = true
)
