package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.model.User
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyOption
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import ar.ort.edu.proyecto_final_grupo_4.services.MedicationSchedulerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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

    fun addMedicationWithScheduleAndFrequency(
        medication: Medication,
        frequency: FrequencyOption,
        startTime: LocalTime,
        selectedWeekDays: List<Int>,
        scheduleVM: ScheduleViewModel,
    ) {
        viewModelScope.launch {
            try {
                // 1. Insertar medicamento
                val medicationId = insertMedicationAndReturnId(medication)

                // 2. Crear primer horario base
                val baseSchedule = Schedule(
                    scheduleID = 0,
                    medicationID = medicationId,
                    frequencyType = frequency.frequencyType,
                    intervalValue = frequency.intervalValue,
                    startTime = startTime,
                    endTime = calculateEndTime(frequency, startTime),
                    isActive = true,
                    startDate = LocalDate.now()
                )

                val baseScheduleId = scheduleVM.addScheduleAndReturnId(baseSchedule)

                // 3. Agregar días de la semana
                when (frequency.frequencyType) {
                    FrequencyType.WEEKLY -> {
                        if (selectedWeekDays.isNotEmpty()) {
                            scheduleVM.addWeekDays(baseScheduleId, selectedWeekDays)
                        }
                    }
                    FrequencyType.DAILY -> {
                        scheduleVM.addWeekDays(baseScheduleId, (0..6).toList())
                    }
                    else -> { /* nada que agregar */ }
                }

                schedulerService.rescheduleMedication(baseScheduleId)

                // 5. Generar horarios adicionales (si corresponde)
                val additionalTimes = generateAdditionalTimes(frequency, startTime)

                for (time in additionalTimes) {
                    val additionalSchedule = baseSchedule.copy(
                        scheduleID = 0,
                        startTime = time
                    )
                    val additionalScheduleId = scheduleVM.addScheduleAndReturnId(additionalSchedule)

                    // Repetimos días de la semana para horarios extra
                    when (frequency.frequencyType) {
                        FrequencyType.WEEKLY -> {
                            if (selectedWeekDays.isNotEmpty()) {
                                scheduleVM.addWeekDays(additionalScheduleId, selectedWeekDays)
                            }
                        }
                        FrequencyType.DAILY -> {
                            scheduleVM.addWeekDays(additionalScheduleId, (0..6).toList())
                        }
                        else -> { /* nada */ }
                    }

                    // Programar la alarma también
                    schedulerService.rescheduleMedication(additionalScheduleId)
                }

                // 6. Actualizar UI si es necesario
                loadMedications(medication.userID)

            } catch (e: Exception) {
                Log.e("MedicationViewModel", "Error adding medication with frequency", e)
            }
        }
    }


    private fun calculateEndTime(frequency: FrequencyOption, startTime: LocalTime): LocalTime? {
        return when (frequency.frequencyType) {
            FrequencyType.TIMES_PER_DAY -> {
                startTime.plusHours(12)
            }
            FrequencyType.HOURS_INTERVAL -> {
                null
            }
            else -> null
        }
    }

    private fun generateAdditionalTimes(frequency: FrequencyOption, startTime: LocalTime): List<LocalTime> {
        return when (frequency.frequencyType) {
            FrequencyType.TIMES_PER_DAY -> {
                val times = frequency.intervalValue ?: 1
                if (times <= 1) return emptyList()

                val hoursSpan = 12
                val intervalForOtherDoses = if (times > 1) hoursSpan / (times - 1) else 0

                (1 until times).mapNotNull { i ->
                    val nextTime = startTime.plusHours((intervalForOtherDoses * i).toLong())
                    // Ensure the generated time is after the initial start time, or 00:00 if it's the wrap-around point
                    if (nextTime.isAfter(startTime) || (nextTime == LocalTime.MIDNIGHT && startTime != LocalTime.MIDNIGHT)) {
                        nextTime
                    } else {
                        null
                    }
                }
            }

            FrequencyType.HOURS_INTERVAL -> {
                val intervalHours = frequency.intervalValue ?: 0
                if (intervalHours <= 0 || intervalHours >= 24) return emptyList() // Guard against invalid intervals

                val distinctDailyDoseTimes = mutableSetOf<LocalTime>()
                var currentCycleTime = startTime // Start with the user's defined startTime

                // Generate all dose times within a 24-hour cycle
                // Start from 00:00 and add interval until we wrap around, ensuring all distinct times are captured.
                // Or, more simply, just keep adding interval hours from the startTime until you loop back to startTime.
                do {
                    distinctDailyDoseTimes.add(currentCycleTime)
                    currentCycleTime = currentCycleTime.plusHours(intervalHours.toLong())
                } while (currentCycleTime != startTime) // Loop until we come back to the original start time

                // Remove the original startTime from this list, as it's handled by baseSchedule
                distinctDailyDoseTimes.remove(startTime)

                // Sort them to maintain order (optional, but good for consistency)
                return distinctDailyDoseTimes.sorted()
            }

            else -> emptyList()
        }
    }

    private suspend fun insertMedicationAndReturnId(medication: Medication): Long {
        return medicationRepository.insertMedication(medication)
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

                // Update your local state
                _dosageUnits.value += savedUnit

                // Call the callback with the saved unit
                onSaved(savedUnit)
            } catch (e: Exception) {
                // Handle error appropriately
                Log.e("MedicationViewModel", "Error adding dosage unit", e)
            }
        }
    }
}
