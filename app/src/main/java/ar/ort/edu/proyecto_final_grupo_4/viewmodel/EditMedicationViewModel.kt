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
                        isActive = firstSchedule.isActive
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

                val updatedMedication = Medication(
                    medicationID = currentState.medicationId,
                    name = currentState.name,
                    dosage = currentState.dosage,
                    dosageUnitID = currentState.selectedUnit?.dosageUnitID ?: 0L,
                    userID = medicationRepository.getById(currentState.medicationId)?.userID ?: 1 // Get existing user ID
                )
                medicationRepository.updateMedication(updatedMedication)

                medicationSchedulerService.cancelAlarmsForMedication(currentState.medicationId)

                scheduleRepository.deactivateSchedulesForMedication(
                    medicationId = currentState.medicationId,
                    newIsActive = false,
                    )


                currentState.selectedFrequency?.let { frequency ->
                    recreateMedicationSchedules(
                        medicationId = currentState.medicationId,
                        frequency = frequency,
                        startTime = currentState.startTime,
                        selectedWeekDays = currentState.selectedWeekDays,
                        isActive = currentState.isActive
                    )
                }

                medicationSchedulerService.scheduleAllActiveMedications()


                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.value = MedicationDetailEvent.SaveSuccess

            } catch (e: Exception) {
                Log.e("MedicationDetailVM", "Error saving changes", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error guardando cambios: ${e.message}") }
                _eventFlow.value = MedicationDetailEvent.SaveFailure(e.message ?: "Error desconocido")
            }
        }
    }


    // In MedicationDetailViewModel.kt
// Make sure you have this helper function (copied from AddMedicationViewModel)
    private fun generateAllPotentialDailyTimes(frequency: FrequencyOption, startTime: LocalTime): List<LocalTime> {
        return when (frequency.frequencyType) {
            FrequencyType.TIMES_PER_DAY -> {
                val times = frequency.intervalValue ?: 1
                if (times <= 1) return listOf(startTime)

                val hoursSpan = 12
                val intervalMinutes = if (times > 1) (hoursSpan.toDouble() / (times - 1)) * 60 else 0.0

                (0 until times).map { i ->
                    startTime.plusMinutes((intervalMinutes * i).toLong())
                }.distinct().sorted()
            }

            FrequencyType.HOURS_INTERVAL -> {
                val intervalHours = frequency.intervalValue ?: 0
                if (intervalHours <= 0 || intervalHours >= 24) return listOf(startTime)

                val doses = mutableSetOf<LocalTime>()
                var currentTime = startTime
                do {
                    doses.add(currentTime)
                    currentTime = currentTime.plusHours(intervalHours.toLong())
                } while (currentTime != startTime)

                doses.sorted()
            }

            else -> {
                listOf(startTime)
            }
        }
    }


    private suspend fun recreateMedicationSchedules(
        medicationId: Long,
        frequency: FrequencyOption,
        startTime: LocalTime,
        selectedWeekDays: List<Int>,
        isActive: Boolean // This will be the isActive state for the NEW schedules
    ) {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val nowTime = LocalTime.now() // Current time for filtering today's doses

        // This section now matches the AddMedicationViewModel's logic
        if (frequency.frequencyType == FrequencyType.DAILY ||
            frequency.frequencyType == FrequencyType.WEEKLY ||
            frequency.frequencyType == FrequencyType.AS_NEEDED) {

            // Create a single base schedule that starts today and recurs indefinitely
            val baseSchedule = Schedule(
                scheduleID = 0,
                medicationID = medicationId,
                frequencyType = frequency.frequencyType,
                intervalValue = frequency.intervalValue,
                startTime = startTime,
                endTime = calculateEndTime(frequency, startTime),
                isActive = isActive, // Use the UI's isActive state
                startDate = today, // Starts today
                endDate = null // Recurring indefinitely
            )

            val baseScheduleId = scheduleRepository.insertSchedule(baseSchedule)

            // Add days of the week if applicable
            when (frequency.frequencyType) {
                FrequencyType.WEEKLY -> {
                    if (selectedWeekDays.isNotEmpty()) {
                        addWeekDaysForSchedule(baseScheduleId, selectedWeekDays)
                    }
                }
                FrequencyType.DAILY -> {
                    addWeekDaysForSchedule(baseScheduleId, (0..6).toList()) // All days for DAILY
                }
                else -> { /* AS_NEEDED, no specific days */ }
            }

        } else { // Handle TIMES_PER_DAY and HOURS_INTERVAL with the "today-only" and "recurring from tomorrow" split

            // Get all potential times for a 24-hour cycle based on frequency and startTime
            val allPotentialDailyTimes = generateAllPotentialDailyTimes(frequency, startTime)

            // --- 2. Create Schedules for Today (only for future doses) ---
            // These schedules have a definite end date (today)
            val todayFutureTimes = allPotentialDailyTimes.filter { it.isAfter(nowTime) }

            if (todayFutureTimes.isNotEmpty()) {
                Log.d("MedicationDetailVM", "Creating ${todayFutureTimes.size} schedules for today.")
                for (time in todayFutureTimes) {
                    val todaySchedule = Schedule(
                        scheduleID = 0,
                        medicationID = medicationId,
                        frequencyType = frequency.frequencyType,
                        intervalValue = frequency.intervalValue,
                        startTime = time,
                        endTime = calculateEndTime(frequency, time),
                        isActive = isActive,
                        startDate = today, // This schedule is only for today
                        endDate = today    // Ends today
                    )
                    scheduleRepository.insertSchedule(todaySchedule)
                    // No need for addWeekDays for these types based on your original logic
                }
            }

            // --- 3. Create Schedules for Recurring (starting tomorrow) ---
            // These schedules recur indefinitely
            Log.d("MedicationDetailVM", "Creating ${allPotentialDailyTimes.size} recurring schedules starting tomorrow.")
            for (time in allPotentialDailyTimes) { // Use all times for the recurring pattern
                val recurringSchedule = Schedule(
                    scheduleID = 0,
                    medicationID = medicationId,
                    frequencyType = frequency.frequencyType,
                    intervalValue = frequency.intervalValue,
                    startTime = time,
                    endTime = calculateEndTime(frequency, time),
                    isActive = isActive,
                    startDate = tomorrow, // This schedule starts tomorrow
                    endDate = null        // And recurs indefinitely
                )
                scheduleRepository.insertSchedule(recurringSchedule)
                // No need for addWeekDays for these types
            }
        }

        // Schedule all active medications (newly created ones).
        // This should be done once at the very end after all schedules are inserted.
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