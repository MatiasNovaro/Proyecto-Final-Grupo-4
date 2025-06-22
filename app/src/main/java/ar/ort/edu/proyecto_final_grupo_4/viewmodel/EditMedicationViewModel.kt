package ar.ort.edu.proyecto_final_grupo_4.presentation.viewmodel // Corrected package name

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.* // Import your models
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.*
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyOption
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.getCommonFrequencies
import ar.ort.edu.proyecto_final_grupo_4.services.MedicationSchedulerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.LocalDate
import java.time.LocalDateTime // Ensure this is imported
import javax.inject.Inject

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val dosageUnitRepository: DosageUnitRepository,
    private val dayOfWeekRepository: DayOfWeekRepository,
    private val medicationSchedulerService: MedicationSchedulerService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditMedicationUiState())
    val uiState: StateFlow<EditMedicationUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableStateFlow<MedicationDetailEvent?>(null)
    val eventFlow: StateFlow<MedicationDetailEvent?> = _eventFlow.asStateFlow()

    // No longer strictly needed as we rebuild the FrequencyOption,
    // but useful for debugging or more complex "diffing" later.
    private var originalFrequencyType: FrequencyType? = null
    private var originalIntervalValue: Int? = null


    fun loadMedicationDetails(medicationId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val medication = medicationRepository.getById(medicationId)
                if (medication == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Medicamento no encontrado.") }
                    return@launch
                }

                // Get all schedules for the medication to infer the overall pattern
                // Assuming getSchedulesForMedication returns List<Schedule> directly (not Flow)
                val allSchedules = scheduleRepository.getSchedulesForMedication(medicationId)
                val firstSchedule = allSchedules.firstOrNull() // Use any schedule to get base properties

                if (firstSchedule == null) {
                    // This scenario means a medication exists but has no schedules,
                    // which shouldn't happen if schedules are always created with meds.
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Horario no encontrado para este medicamento.") }
                    return@launch
                }

                val allUnits = dosageUnitRepository.getAllUnits()
                val selectedUnit = allUnits.find { it.dosageUnitID == medication.dosageUnitID }

                // Infer FrequencyOption from the first schedule's properties
                val commonFrequencies = getCommonFrequencies()
                val inferredFrequencyOption = commonFrequencies.find {
                    it.frequencyType == firstSchedule.frequencyType && it.intervalValue == firstSchedule.intervalValue
                } ?: run {
                    // If it's a custom frequency, construct a FrequencyOption for display
                    FrequencyOption(
                        id = "custom_${firstSchedule.frequencyType.name}",
                        displayName = firstSchedule.frequencyType.displayName + (firstSchedule.intervalValue?.let { " cada $it" } ?: ""),
                        frequencyType = firstSchedule.frequencyType,
                        intervalValue = firstSchedule.intervalValue,
                        description = "Frecuencia personalizada" // Or derive from description if available in Schedule
                    )
                }

                val selectedWeekDays = if (firstSchedule.frequencyType == FrequencyType.WEEKLY) {
                    // Fetch days of week associated with any of the schedules (assuming consistent for the pattern)
                    // You might need a more robust way to get ALL weekdays if multiple schedules exist for WEEKLY
                    // and they have different weekday sets, but usually one set applies to the pattern.
                    // For now, we take from the first schedule's ID.
                    dayOfWeekRepository.getDaysForSchedule(firstSchedule.scheduleID).map { it.dayOfWeek }
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        medicationId = medication.medicationID,
                        name = medication.name,
                        dosage = medication.dosage,
                        selectedUnit = selectedUnit,
                        availableDosageUnits = allUnits,
                        selectedFrequency = inferredFrequencyOption,
                        selectedWeekDays = selectedWeekDays,
                        startTime = firstSchedule.startTime,
                        isActive = firstSchedule.isActive // Assume active status is for the overall medication pattern
                    )
                }
            } catch (e: Exception) {
                Log.e("MedicationDetailVM", "Error loading medication details", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error cargando detalles: ${e.message}") }
            }
        }
    }

    // --- UI State Update Functions ---
    fun updateName(newName: String) { _uiState.update { it.copy(name = newName) } }
    fun updateDosage(newDosage: String) { _uiState.update { it.copy(dosage = newDosage) } }
    fun updateSelectedUnit(newUnit: DosageUnit) { _uiState.update { it.copy(selectedUnit = newUnit) } }
    fun updateSelectedFrequency(newFrequency: FrequencyOption) { _uiState.update { it.copy(selectedFrequency = newFrequency) } }
    fun updateSelectedWeekDays(newDays: List<Int>) { _uiState.update { it.copy(selectedWeekDays = newDays) } }
    fun updateStartTime(newTime: LocalTime) { _uiState.update { it.copy(startTime = newTime) } }
    fun updateIsActive(newIsActive: Boolean) { _uiState.update { it.copy(isActive = newIsActive) } }

    fun addDosageUnit(newUnitName: String, onUnitSaved: (DosageUnit) -> Unit) {
        viewModelScope.launch {
            try {
                val newUnit = DosageUnit(name = newUnitName)
                val unitId = dosageUnitRepository.insertUnit(newUnit)
                val savedUnit = newUnit.copy(dosageUnitID = unitId)
                _uiState.update { it.copy(availableDosageUnits = it.availableDosageUnits + savedUnit, selectedUnit = savedUnit) }
                onUnitSaved(savedUnit)
            } catch (e: Exception) {
                Log.e("MedicationDetailVM", "Error adding dosage unit", e)
                _eventFlow.value = MedicationDetailEvent.SaveFailure("Error al agregar unidad: ${e.message}")
            }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentState = uiState.value

                // 1. Update Medication (existing ID)
                val updatedMedication = Medication(
                    medicationID = currentState.medicationId,
                    name = currentState.name,
                    dosage = currentState.dosage,
                    dosageUnitID = currentState.selectedUnit?.dosageUnitID ?: 0L,
                    // Assume userID doesn't change on edit, retrieve from existing medication
                    userID = medicationRepository.getById(currentState.medicationId)?.userID ?: 1
                )
                medicationRepository.updateMedication(updatedMedication)

                // 2. Clear ALL existing schedules and alarms for this medication
                // Important: Cancel alarms BEFORE deleting schedules if alarm manager relies on schedule IDs
                medicationSchedulerService.cancelAlarmsForMedication(currentState.medicationId)
                scheduleRepository.deleteSchedulesForMedication(currentState.medicationId)


                // 3. Re-create new schedules and alarms using the shared logic
                currentState.selectedFrequency?.let { frequency ->
                    recreateMedicationSchedules(
                        medicationId = currentState.medicationId,
                        frequency = frequency,
                        startTime = currentState.startTime,
                        selectedWeekDays = currentState.selectedWeekDays,
                        isActive = currentState.isActive
                    )
                }

                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.value = MedicationDetailEvent.SaveSuccess

            } catch (e: Exception) {
                Log.e("MedicationDetailVM", "Error saving changes", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error guardando cambios: ${e.message}") }
                _eventFlow.value = MedicationDetailEvent.SaveFailure(e.message ?: "Error desconocido")
            }
        }
    }


    // --- Shared Schedule Generation Logic (adapted from your addMedicationWithScheduleAndFrequency) ---

    private suspend fun recreateMedicationSchedules(
        medicationId: Long,
        frequency: FrequencyOption,
        startTime: LocalTime,
        selectedWeekDays: List<Int>,
        isActive: Boolean
    ) {
        // 1. Create base schedule
        val baseSchedule = Schedule(
            scheduleID = 0, // Will be auto-generated
            medicationID = medicationId,
            frequencyType = frequency.frequencyType,
            intervalValue = frequency.intervalValue,
            startTime = startTime,
            endTime = calculateEndTime(frequency, startTime),
            isActive = isActive, // Use the UI's isActive state
            startDate = LocalDate.now() // Or retrieve and use the original startDate if desired
        )

        val baseScheduleId = scheduleRepository.insertSchedule(baseSchedule)

        // 2. Add weekdays (if applicable)
        when (frequency.frequencyType) {
            FrequencyType.WEEKLY -> {
                if (selectedWeekDays.isNotEmpty()) {
                    // Use a new function or directly insert DayOfWeek
                    addWeekDaysForSchedule(baseScheduleId, selectedWeekDays)
                }
            }
            FrequencyType.DAILY -> {
                // DAILY schedules imply all days (0-6)
                addWeekDaysForSchedule(baseScheduleId, (0..6).toList())
            }
            else -> { /* No specific days for other types */ }
        }

        // Schedule alarm for the base schedule (this needs to be handled by the scheduler service)
        // Note: medicationSchedulerService.scheduleAllActiveMedications() will be called at the end
        // to re-evaluate all alarms, including the newly created ones.

        // 3. Generate additional schedules (if any)
        val additionalTimes = generateAdditionalTimes(frequency, startTime)

        for (time in additionalTimes) {
            val additionalSchedule = baseSchedule.copy(
                scheduleID = 0, // New ID for each additional schedule
                startTime = time,
                isActive = isActive // Ensure additional schedules also use the overall isActive state
            )
            val additionalScheduleId = scheduleRepository.insertSchedule(additionalSchedule)

            // Re-add weekdays for additional schedules (if applicable)
            when (frequency.frequencyType) {
                FrequencyType.WEEKLY -> {
                    if (selectedWeekDays.isNotEmpty()) {
                        addWeekDaysForSchedule(additionalScheduleId, selectedWeekDays)
                    }
                }
                FrequencyType.DAILY -> {
                    addWeekDaysForSchedule(additionalScheduleId, (0..6).toList())
                }
                else -> { /* nada */ }
            }
            // Alarms for additional schedules will also be handled by scheduleAllActiveMedications()
        }

        // After all schedules are inserted, re-schedule all alarms for the medication.
        // This is crucial because old alarms were cancelled and new schedules need new alarms.
        medicationSchedulerService.scheduleAllActiveMedications()
    }

    // Helper for adding weekdays (adapted from ScheduleViewModel)
    private suspend fun addWeekDaysForSchedule(scheduleId: Long, days: List<Int>) {
        days.forEach { dayInt ->
            dayOfWeekRepository.insertDayOfWeek(DayOfWeek(scheduleID = scheduleId, dayOfWeek = dayInt))
        }
    }


    // --- Private Helper Functions (Copied from your add medication logic) ---

    private fun calculateEndTime(frequency: FrequencyOption, startTime: LocalTime): LocalTime? {
        return when (frequency.frequencyType) {
            // Your logic
            FrequencyType.TIMES_PER_DAY -> {
                startTime.plusHours(12) // Example: 12-hour span for doses
            }
            // For HOUS_INTERVAL and other types, endTime is typically null
            FrequencyType.HOURS_INTERVAL -> null
            else -> null
        }
    }

    private fun generateAdditionalTimes(frequency: FrequencyOption, startTime: LocalTime): List<LocalTime> {
        return when (frequency.frequencyType) {
            FrequencyType.TIMES_PER_DAY -> {
                val times = frequency.intervalValue ?: 1
                if (times <= 1) return emptyList()

                val hoursSpan = 12 // Your defined span for TIMES_PER_DAY
                val intervalForOtherDoses = if (times > 1) hoursSpan / (times - 1) else 0

                (1 until times).mapNotNull { i ->
                    val nextTime = startTime.plusHours((intervalForOtherDoses * i).toLong())
                    // Ensure the generated time is after the initial start time, or handles midnight wrap-around
                    if (nextTime.isAfter(startTime) || (nextTime == LocalTime.MIDNIGHT && startTime != LocalTime.MIDNIGHT)) {
                        nextTime
                    } else {
                        // This else block might mean you're not generating enough doses for the span
                        // or that the interval is too large for the remaining span.
                        // You might need to adjust the `hoursSpan` or the interval calculation
                        // if you expect more doses to be generated beyond 24:00.
                        null
                    }
                }
            }

            FrequencyType.HOURS_INTERVAL -> {
                val intervalHours = frequency.intervalValue ?: 0
                if (intervalHours <= 0 || intervalHours >= 24) return emptyList()

                val distinctDailyDoseTimes = mutableSetOf<LocalTime>()
                var currentCycleTime = startTime

                // Generate times for a 24-hour cycle
                // Loop until we come back to the start time (or just before)
                // We add the starting time in the main schedule and then generate additional times
                for (i in 1..24 / intervalHours) { // Generate up to 24 hours of doses
                    currentCycleTime = startTime.plusHours((intervalHours * i).toLong())
                    if (currentCycleTime == startTime && i > 0) break // Stop if we loop back to start time
                    distinctDailyDoseTimes.add(currentCycleTime)
                }

                // Remove the initial startTime if it was accidentally added or if it's implicitly handled by the base schedule.
                // The base schedule already covers the `startTime`.
                distinctDailyDoseTimes.remove(startTime) // Ensure the start time isn't duplicated if generated by accident

                return distinctDailyDoseTimes.sorted()
            }

            else -> emptyList()
        }
    }

    fun clearEvent() {
        _eventFlow.value = null
    }
}

sealed class MedicationDetailEvent {
    object SaveSuccess : MedicationDetailEvent()
    data class SaveFailure(val message: String) : MedicationDetailEvent()
}