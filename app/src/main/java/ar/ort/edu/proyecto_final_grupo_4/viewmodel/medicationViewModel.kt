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
    private val dayOfWeekRepository: DayOfWeekRepository

) : ViewModel() {
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications = _medications.asStateFlow()

    private val _dosageUnits = MutableStateFlow<List<DosageUnit>>(emptyList())
    val dosageUnits: StateFlow<List<DosageUnit>> = _dosageUnits
    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog = _showErrorDialog.asStateFlow()


    fun loadMedications(userId: Int) {
        viewModelScope.launch {
            _medications.value = medicationRepository.getMedicationsByUser(userId)
        }
    }

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.insertMedication(medication)
            loadMedications(medication.userID)
        }
    }

    // Método anterior para compatibilidad
    fun addMedicationWithSchedule(
        medication: Medication,
        time: LocalTime,
        scheduleViewModel: ScheduleViewModel
    ) {
        viewModelScope.launch {
            val medicationId = insertMedicationAndReturnId(medication)
            val schedule = Schedule(
                scheduleID = 0,
                medicationID = medicationId,
                frequencyType = FrequencyType.DAILY, // Por defecto diario
                startTime = time,
                isActive = true
            )
            scheduleViewModel.addSchedule(schedule)
            loadMedications(medication.userID)
        }
    }

    fun addMedicationWithScheduleAndFrequency(
        medication: Medication,
        frequency: FrequencyOption,
        startTime: LocalTime,
        selectedWeekDays: List<Int>,
        scheduleVM: ScheduleViewModel
    ) {
        viewModelScope.launch {
            try {
                val medicationId = insertMedicationAndReturnId(medication)

                val schedule = Schedule(
                    scheduleID = 0,
                    medicationID = medicationId,
                    frequencyType = frequency.frequencyType,
                    intervalValue = frequency.intervalValue,
                    startTime = startTime,
                    endTime = calculateEndTime(frequency, startTime),
                    isActive = true,
                    startDate = LocalDate.now()
                )

                val scheduleId = scheduleVM.addScheduleAndReturnId(schedule)

                if (frequency.frequencyType == FrequencyType.WEEKLY && selectedWeekDays.isNotEmpty()) {
                    scheduleVM.addWeekDays(scheduleId, selectedWeekDays)
                } else if (frequency.frequencyType == FrequencyType.DAILY) {
                    scheduleVM.addWeekDays(scheduleId, (0..6).toList())
                }

                val additionalTimes = generateAdditionalTimes(frequency, startTime)
                additionalTimes.forEach { time ->
                    val additionalSchedule = schedule.copy(
                        scheduleID = 0,
                        startTime = time
                    )
                    val additionalScheduleId = scheduleVM.addScheduleAndReturnId(additionalSchedule)

                    if (frequency.frequencyType == FrequencyType.WEEKLY && selectedWeekDays.isNotEmpty()) {
                        scheduleVM.addWeekDays(additionalScheduleId, selectedWeekDays)
                    } else if (frequency.frequencyType == FrequencyType.DAILY) {
                        scheduleVM.addWeekDays(additionalScheduleId, (0..6).toList())
                    }
                }

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

                val hoursInterval = 12 / (times - 1)
                (1 until times).map { i ->
                    startTime.plusHours((hoursInterval * i).toLong())
                }
            }

            FrequencyType.HOURS_INTERVAL -> {
                val intervalHours = frequency.intervalValue ?: 24
                if (intervalHours >= 24) return emptyList()

                val timesPerDay = 24 / intervalHours
                (1 until timesPerDay).map { i ->
                    startTime.plusHours((intervalHours * i).toLong())
                }
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

    fun addDosageUnit(unit: DosageUnit) {
        viewModelScope.launch {
            try {
                val savedUnitId = dosageUnitRepository.insertUnit(unit)
                val savedUnit = unit.copy(dosageUnitID = savedUnitId)
                _dosageUnits.value += savedUnit
            } catch (e: Exception) {
                Log.e("MedicationViewModel", "Error adding dosage unit", e)
            }
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
