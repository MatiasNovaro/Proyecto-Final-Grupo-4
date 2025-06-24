package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.model.EditMedicationUiState
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyOption
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import ar.ort.edu.proyecto_final_grupo_4.presentation.viewmodel.MedicationDetailEvent
import ar.ort.edu.proyecto_final_grupo_4.services.MedicationSchedulerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val dosageUnitRepository: DosageUnitRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val scheduleRepository: ScheduleRepository,
    private val dayOfWeekRepository: DayOfWeekRepository,
    private val schedulerService: MedicationSchedulerService

) : ViewModel() {
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications = _medications.asStateFlow()

    private val _dosageUnits = MutableStateFlow<List<DosageUnit>>(emptyList())
    val dosageUnits: StateFlow<List<DosageUnit>> = _dosageUnits
    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog = _showErrorDialog.asStateFlow()

    fun checkMedicationNameExists(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = medicationRepository.existsByName(name)
            onResult(exists)
        }
    }


    fun programAlarm(scheduleId: Long) {
        viewModelScope.launch {
            schedulerService.rescheduleMedication(scheduleId)
        }
    }
    fun loadMedications(userId: Int) {
        viewModelScope.launch {
            _medications.value = medicationRepository.getMedicationsByUser(userId)
        }
    }


//    fun addMedicationWithScheduleAndFrequency(
//        medication: Medication,
//        frequency: FrequencyOption,
//        startTime: LocalTime,
//        selectedWeekDays: List<Int>,
//        scheduleVM: ScheduleViewModel,
//    ) {
//        viewModelScope.launch {
//            try {
//                // 1. Insertar medicamento
//                val medicationId = insertMedicationAndReturnId(medication)
//
//                // 2. Crear primer horario base
//                val baseSchedule = Schedule(
//                    scheduleID = 0,
//                    medicationID = medicationId,
//                    frequencyType = frequency.frequencyType,
//                    intervalValue = frequency.intervalValue,
//                    startTime = startTime,
//                    endTime = calculateEndTime(frequency, startTime),
//                    isActive = true,
//                    startDate = LocalDate.now()
//                )
//
//                val baseScheduleId = scheduleVM.addScheduleAndReturnId(baseSchedule)
//
//                // 3. Agregar días de la semana
//                when (frequency.frequencyType) {
//                    FrequencyType.WEEKLY -> {
//                        if (selectedWeekDays.isNotEmpty()) {
//                            scheduleVM.addWeekDays(baseScheduleId, selectedWeekDays)
//                        }
//                    }
//                    FrequencyType.DAILY -> {
//                        scheduleVM.addWeekDays(baseScheduleId, (0..6).toList())
//                    }
//                    else -> { /* nada que agregar */ }
//                }
//
//                schedulerService.rescheduleMedication(baseScheduleId)
//
//                // 5. Generar horarios adicionales (si corresponde)
//                val additionalTimes = generateAdditionalTimes(frequency, startTime)
//
//                for (time in additionalTimes) {
//                    val additionalSchedule = baseSchedule.copy(
//                        scheduleID = 0,
//                        startTime = time
//                    )
//                    val additionalScheduleId = scheduleVM.addScheduleAndReturnId(additionalSchedule)
//
//                    // Repetimos días de la semana para horarios extra
//                    when (frequency.frequencyType) {
//                        FrequencyType.WEEKLY -> {
//                            if (selectedWeekDays.isNotEmpty()) {
//                                scheduleVM.addWeekDays(additionalScheduleId, selectedWeekDays)
//                            }
//                        }
//                        FrequencyType.DAILY -> {
//                            scheduleVM.addWeekDays(additionalScheduleId, (0..6).toList())
//                        }
//                        else -> { /* nada */ }
//                    }
//
//                    // Programar la alarma también
//                    schedulerService.rescheduleMedication(additionalScheduleId)
//                }
//
//                // 6. Actualizar UI si es necesario
//                loadMedications(medication.userID)
//
//            } catch (e: Exception) {
//                Log.e("MedicationViewModel", "Error adding medication with frequency", e)
//            }
//        }
//    }
//
//
//    private fun calculateEndTime(frequency: FrequencyOption, startTime: LocalTime): LocalTime? {
//        return when (frequency.frequencyType) {
//            FrequencyType.TIMES_PER_DAY -> {
//                startTime.plusHours(12)
//            }
//            FrequencyType.HOURS_INTERVAL -> {
//                null
//            }
//            else -> null
//        }
//    }
//
//    private fun generateAdditionalTimes(frequency: FrequencyOption, startTime: LocalTime): List<LocalTime> {
//        return when (frequency.frequencyType) {
//            FrequencyType.TIMES_PER_DAY -> {
//                val times = frequency.intervalValue ?: 1
//                if (times <= 1) return emptyList()
//
//                val hoursSpan = 12
//                val intervalForOtherDoses = if (times > 1) hoursSpan / (times - 1) else 0
//
//                (1 until times).mapNotNull { i ->
//                    val nextTime = startTime.plusHours((intervalForOtherDoses * i).toLong())
//                    // Ensure the generated time is after the initial start time, or 00:00 if it's the wrap-around point
//                    if (nextTime.isAfter(startTime) || (nextTime == LocalTime.MIDNIGHT && startTime != LocalTime.MIDNIGHT)) {
//                        nextTime
//                    } else {
//                        null
//                    }
//                }
//            }
//
//            FrequencyType.HOURS_INTERVAL -> {
//                val intervalHours = frequency.intervalValue ?: 0
//                if (intervalHours <= 0 || intervalHours >= 24) return emptyList() // Guard against invalid intervals
//
//                val distinctDailyDoseTimes = mutableSetOf<LocalTime>()
//                var currentCycleTime = startTime // Start with the user's defined startTime
//
//                do {
//                    distinctDailyDoseTimes.add(currentCycleTime)
//                    currentCycleTime = currentCycleTime.plusHours(intervalHours.toLong())
//                } while (currentCycleTime != startTime)
//
//                distinctDailyDoseTimes.remove(startTime)
//
//                return distinctDailyDoseTimes.sorted()
//            }
//
//            else -> emptyList()
//        }
//    }
//
//    private suspend fun insertMedicationAndReturnId(medication: Medication): Long {
//        return medicationRepository.insertMedication(medication)
//    }

    fun addMedicationWithScheduleAndFrequency(
        medication: Medication,
        frequency: FrequencyOption,
        startTime: LocalTime,
        selectedWeekDays: List<Int>,
        scheduleVM: ScheduleViewModel, // Assuming scheduleVM has addScheduleAndReturnId and addWeekDays
    ) {
        viewModelScope.launch {
            try {
                // 1. Insertar medicamento
                val medicationId = insertMedicationAndReturnId(medication)

                val today = LocalDate.now()
                val tomorrow = today.plusDays(1)
                val nowTime = LocalTime.now() // Current time for filtering today's doses

                // Handle DAILY, WEEKLY, AS_NEEDED first as single, recurring schedules from today
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
                        isActive = true,
                        startDate = today, // Starts today
                        endDate = null // Recurring indefinitely
                    )

                    val baseScheduleId = scheduleVM.addScheduleAndReturnId(baseSchedule)

                    // Add days of the week if applicable
                    when (frequency.frequencyType) {
                        FrequencyType.WEEKLY -> {
                            if (selectedWeekDays.isNotEmpty()) {
                                scheduleVM.addWeekDays(baseScheduleId, selectedWeekDays)
                            }
                        }
                        FrequencyType.DAILY -> {
                            scheduleVM.addWeekDays(baseScheduleId, (0..6).toList()) // All days for DAILY
                        }
                        else -> { /* AS_NEEDED, no specific days */ }
                    }

                    // Schedule alarms for this single recurring schedule
                    schedulerService.rescheduleMedication(baseScheduleId)

                } else { // Handle TIMES_PER_DAY and HOURS_INTERVAL with the "today-only" and "recurring from tomorrow" split

                    // Get all potential times for a 24-hour cycle based on frequency and startTime
                    val allPotentialDailyTimes = generateAllPotentialDailyTimes(frequency, startTime)

                    // --- 2. Create Schedules for Today (only for future doses) ---
                    val todayFutureTimes = allPotentialDailyTimes.filter { it.isAfter(nowTime) }

                    if (todayFutureTimes.isNotEmpty()) {
                        Log.d("AddMed", "Creating ${todayFutureTimes.size} schedules for today.")
                        for (time in todayFutureTimes) {
                            val todaySchedule = Schedule(
                                scheduleID = 0,
                                medicationID = medicationId,
                                frequencyType = frequency.frequencyType,
                                intervalValue = frequency.intervalValue,
                                startTime = time,
                                endTime = calculateEndTime(frequency, time),
                                isActive = true,
                                startDate = today, // This schedule is only for today
                                endDate = today    // Ends today
                            )
                            val todayScheduleId = scheduleVM.addScheduleAndReturnId(todaySchedule)
                            // No need for addWeekDays for these types based on your original logic
                            schedulerService.rescheduleMedication(todayScheduleId)
                        }
                    }

                    // --- 3. Create Schedules for Recurring (starting tomorrow) ---
                    Log.d("AddMed", "Creating ${allPotentialDailyTimes.size} recurring schedules starting tomorrow.")
                    for (time in allPotentialDailyTimes) { // Use all times for the recurring pattern
                        val recurringSchedule = Schedule(
                            scheduleID = 0,
                            medicationID = medicationId,
                            frequencyType = frequency.frequencyType,
                            intervalValue = frequency.intervalValue,
                            startTime = time,
                            endTime = calculateEndTime(frequency, time),
                            isActive = true,
                            startDate = tomorrow, // This schedule starts tomorrow
                            endDate = null        // And recurs indefinitely
                        )
                        val recurringScheduleId = scheduleVM.addScheduleAndReturnId(recurringSchedule)
                        // No need for addWeekDays for these types
                        schedulerService.rescheduleMedication(recurringScheduleId)
                    }
                }

                // 4. Actualizar UI si es necesario (assuming loadMedications exists and updates your list)
                loadMedications(medication.userID)

            } catch (e: Exception) {
                Log.e("MedicationViewModel", "Error adding medication with frequency: ${e.message}", e)
                // You might want to provide user feedback here
            }
        }
    }

// --- Helper functions (ensure these are in the same scope or accessible) ---

    // This function calculates the end time for a schedule, which might be dependent
// on the frequency type and start time.
    private fun calculateEndTime(frequency: FrequencyOption, startTime: LocalTime): LocalTime? {
        return when (frequency.frequencyType) {
            FrequencyType.TIMES_PER_DAY -> {
                // If TIMES_PER_DAY implies a specific span (e.g., 12 hours from start)
                startTime.plusHours(12)
            }
            FrequencyType.HOURS_INTERVAL -> {
                null // Typically no fixed end time for interval-based continuous schedules
            }
            else -> null
        }
    }

    /**
     * Generates all potential LocalTime instances for a given frequency type (HOURS_INTERVAL, TIMES_PER_DAY)
     * within a 24-hour cycle, starting from the base startTime.
     * This function does NOT filter for past times; it returns the full pattern.
     */
    private fun generateAllPotentialDailyTimes(frequency: FrequencyOption, startTime: LocalTime): List<LocalTime> {
        return when (frequency.frequencyType) {
            FrequencyType.TIMES_PER_DAY -> {
                val times = frequency.intervalValue ?: 1
                if (times <= 1) return listOf(startTime) // If only 1 time, it's just the start time

                // Distribute 'times' doses over a 12-hour span (matching calculateEndTime)
                val hoursSpan = 12
                val intervalMinutes = if (times > 1) (hoursSpan.toDouble() / (times - 1)) * 60 else 0.0

                (0 until times).map { i ->
                    startTime.plusMinutes((intervalMinutes * i).toLong())
                }.distinct().sorted() // Ensure unique times and sorted order
            }

            FrequencyType.HOURS_INTERVAL -> {
                val intervalHours = frequency.intervalValue ?: 0
                if (intervalHours <= 0 || intervalHours >= 24) return listOf(startTime) // Handle invalid/single intervals

                val doses = mutableSetOf<LocalTime>()
                var currentTime = startTime
                // Loop for a full 24-hour cycle
                do {
                    doses.add(currentTime)
                    currentTime = currentTime.plusHours(intervalHours.toLong())
                } while (currentTime != startTime) // Stop when we loop back to the start time

                doses.sorted() // Return sorted list of all unique times
            }

            else -> {
                // This function is intended for TIMES_PER_DAY and HOURS_INTERVAL.
                // For other types, this path shouldn't be taken with the new logic,
                // but return the startTime as a fallback for safety.
                listOf(startTime)
            }
        }
    }

    private suspend fun insertMedicationAndReturnId(medication: Medication): Long {
        // This function needs access to your medicationRepository
        return medicationRepository.insertMedication(medication)
    }

    suspend fun getScheduleByMedicationId(medicationId: Long): List<Schedule> {
        return  scheduleRepository.getSchedulesForMedication(medicationId)
    }
    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            val logs = medicationLogRepository.getMedicationLogs(medication.medicationID)
            if (logs.isEmpty()) {
                try {
                    scheduleRepository.getSchedulesForMedication(medication.medicationID).forEach { schedule ->
                        dayOfWeekRepository.deleteDaysForSchedule(schedule.scheduleID)
                    }
                    scheduleRepository.deleteSchedulesForMedication(medication.medicationID)
                    medicationRepository.deleteMedication(medication)
                    loadMedications(medication.userID)
                } catch (e: Exception) {
                    Log.e("MedicationViewModel", "Error deleting medication", e)
                }
            } else {
                _showErrorDialog.value = true
            }
        }
    }
    fun dismissErrorDialog() {
        _showErrorDialog.value = false
    }

    fun loadDosageUnits() {
        viewModelScope.launch {
            _dosageUnits.value = dosageUnitRepository.getAllUnits()
        }
    }

    fun addDosageUnit(unit: DosageUnit, onSaved: (DosageUnit) -> Unit) {
        viewModelScope.launch {
            try {
                val savedUnitId = dosageUnitRepository.insertUnit(unit)
                val savedUnit = unit.copy(dosageUnitID = savedUnitId)

                _dosageUnits.value += savedUnit

                onSaved(savedUnit)
            } catch (e: Exception) {
                Log.e("MedicationViewModel", "Error adding dosage unit", e)
            }
        }
    }

}
