package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DayOfWeek
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DosageUnit
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationStatus
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduleWithMedication
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val dayOfWeekRepository: DayOfWeekRepository,
    private val medicationRepository: MedicationRepository,
    private val dosageUnitRepository: DosageUnitRepository,
    private val medicationlogRepository: MedicationLogRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    private val _todaySchedules = MutableStateFlow<List<ScheduleWithDetails>>(emptyList())
    val todaySchedules: StateFlow<List<ScheduleWithDetails>> = _todaySchedules


    // Método anterior para compatibilidad
    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.insertSchedule(schedule)
            loadSchedules() // Si tienes este método
        }
    }
   suspend fun getMedicationsByScheduleIds(scheduleIds: List<Long>): Flow<List<ScheduleWithMedication>> {
        return scheduleRepository.getSchedulesWithMedicationsByIds(scheduleIds)
    }
     fun getSchedulesWithMedications(scheduleIds: List<Long>): Flow<List<ScheduleWithMedication>> {
        return scheduleRepository.getSchedulesWithMedicationsByIds(scheduleIds)
    }

    fun markAsTaken(scheduleId: Long) {
        viewModelScope.launch {
            // Update schedule status
            scheduleRepository.updateScheduleStatus(scheduleId, MedicationStatus.TAKEN)

            // Create and save a MedicationLog entry for "taken"
            val medicationLog = MedicationLog(
                scheduleID = scheduleId,
                wasTaken = true,
                timestamp = LocalDateTime.now() // Record the current time
            )
            medicationlogRepository.insertLog(medicationLog)

            // You might want to also cancel the specific alarm here if not handled elsewhere
            // Example: medicationAlarmManager.cancelAlarm(scheduleId)
        }
    }

    fun markAsSkipped(scheduleId: Long) {
        viewModelScope.launch {
            // Update schedule status
            scheduleRepository.updateScheduleStatus(scheduleId, MedicationStatus.SKIPPED)

            // Create and save a MedicationLog entry for "skipped"
            val medicationLog = MedicationLog(
                scheduleID = scheduleId,
                wasTaken = false,
                timestamp = LocalDateTime.now() // Record the current time
            )
            medicationlogRepository.insertLog(medicationLog)
        }
    }
    fun snoozeAlarm(scheduleId: Long, minutes: Int) {
        viewModelScope.launch {
            // Your existing snooze logic, which will reschedule the alarm
            // (You'll need an instance of MedicationAlarmManager in ViewModel for this,
            // or pass it via dependency injection)
            // Example: medicationAlarmManager.snoozeAlarm(scheduleId, minutes)
        }
    }
    // Nuevo método que retorna el ID del schedule insertado
    suspend fun addScheduleAndReturnId(schedule: Schedule): Long {
        return scheduleRepository.insertSchedule(schedule)
    }

    // Método para agregar días de la semana a un schedule
    suspend fun addWeekDays(scheduleId: Long, weekDays: List<Int>) {
        weekDays.forEach { dayOfWeek ->
            val dayEntry = DayOfWeek(
                scheduleID = scheduleId,
                dayOfWeek = dayOfWeek
            )
            dayOfWeekRepository.insertDayOfWeek(dayEntry)
        }
    }

    // Método para obtener schedules con sus días
    fun loadSchedulesForMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _schedules.value = scheduleRepository.getSchedulesForMedication(medicationId)
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading schedules", e)
            }
        }
    }

    // Método para obtener todos los schedules
    fun loadSchedules() {
        viewModelScope.launch {
            try {
                _schedules.value = scheduleRepository.getAllSchedules()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading all schedules", e)
            }
        }
    }

    // Método para obtener schedules de hoy
    fun loadTodaySchedules() {
        viewModelScope.launch {
            val allSchedules = scheduleRepository.getAllSchedules()
            val today = LocalDate.now()
            val now = LocalDateTime.now() // Get current date and time

            // Get the day of week using java.time.DayOfWeek.getValue() (1=Mon to 7=Sun)
            val dayOfWeekJavaValue = today.dayOfWeek.value

            // Convert to your schema: 0 = Sunday, 1 = Monday, ..., 6 = Saturday
            val currentDayOfWeekInt = if (dayOfWeekJavaValue == 7) 0 else dayOfWeekJavaValue

            val todaySchedules = mutableListOf<ScheduleWithDetails>()

            for (schedule in allSchedules) {
                val isInDateRange = !schedule.startDate.isAfter(today) &&
                        (schedule.endDate == null || !today.isAfter(schedule.endDate))

                if (!isInDateRange) continue

                // Fetch the list of your Room DayOfWeek entities
                val roomWeekDaysForSchedule: List<DayOfWeek> = dayOfWeekRepository.getDaysForSchedule(schedule.scheduleID)

                // Extract the integer dayOfWeek values (0-6) from these entities
                val storedWeekDayInts: List<Int> = roomWeekDaysForSchedule.map { it.dayOfWeek }

                val isScheduledTodayBasedOnWeekDays = when (schedule.frequencyType) {
                    FrequencyType.DAILY,
                    FrequencyType.HOURS_INTERVAL,
                    FrequencyType.TIMES_PER_DAY,
                    FrequencyType.DAYS_INTERVAL,
                    FrequencyType.AS_NEEDED -> true
                    FrequencyType.WEEKLY -> storedWeekDayInts.contains(currentDayOfWeekInt) // Use the converted value here
                    else -> false
                }

                if (!isScheduledTodayBasedOnWeekDays) continue

                val med = medicationRepository.getById(schedule.medicationID)
                val unit = med?.let { dosageUnitRepository.getById(it.dosageUnitID) }

                if (med == null || unit == null) {
                    Log.w("ScheduleVM", "Medication or DosageUnit missing for schedule ID: ${schedule.scheduleID}")
                    continue
                }

                // Only use the specific startTime of THIS schedule entity for the current day
                val potentialDosesForCurrentDay: List<LocalDateTime> = listOf(schedule.startTime.atDate(today))


                for (doseTimeCandidate in potentialDosesForCurrentDay) {
                    val logForThisDose = medicationlogRepository.getSpecificLogForScheduleAndExactTime(
                        schedule.scheduleID,
                        doseTimeCandidate
                    )

                    val wasDoseTaken = logForThisDose?.wasTaken == true
                    println("Dentro dol view $wasDoseTaken")
                    val isFutureDose = doseTimeCandidate.isAfter(now)
                    val isCurrentlyDue = !doseTimeCandidate.isBefore(now.minusMinutes(1)) && doseTimeCandidate.isBefore(now.plusMinutes(15))
                    // Define 'isPast' simply: if it's not future and not currently due, it's past.
                    val isPast = !isFutureDose && !isCurrentlyDue


                    // *** CRITICAL CHANGE: Include all relevant doses for the day, regardless of past taken status ***
                    // We want to show:
                    // 1. Future doses
                    // 2. Currently due doses
                    // 3. Any past doses (whether taken or not, so the user has a full overview)
                    if (isFutureDose || isCurrentlyDue || isPast) {
                        val detail = ScheduleWithDetails(
                            schedule = schedule,
                            medication = med,
                            dosageUnit = unit,
                            weekDays = storedWeekDayInts,
                            nextDose = doseTimeCandidate,
                            isCompletedToday = wasDoseTaken // This flag will now correctly reflect if it was taken
                        )
                        todaySchedules.add(detail)
                    }
                }
            }
            _todaySchedules.value = todaySchedules.sortedBy { it.nextDose }
        }
    }

    // Función auxiliar para generar el label del día
    private fun getDayLabel(targetDate: LocalDate, today: LocalDate): String {
        val daysBetween = ChronoUnit.DAYS.between(targetDate, today)

        return when (daysBetween.toInt()) {
            0 -> "Hoy"
            1 -> "Ayer"
            in 2..6 -> "${daysBetween.toInt()} días atrás"
            else -> targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }

    // Por ahora generar status random, después implementar tracking real
    private fun generateRandomTakenStatus(): Boolean {
        return (0..1).random() == 1
    }

    // Método para marcar una toma como completada
    fun markScheduleAsCompleted(scheduleId: Int, date: LocalDate, time: LocalTime) {
        viewModelScope.launch {
            // Implementar lógica para marcar como completada
            // Probablemente necesites una tabla adicional para tracking de tomas
        }
    }

    // Método para eliminar un schedule
    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            try {
                scheduleRepository.deleteSchedule(scheduleId)
                loadSchedules()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error deleting schedule", e)
            }
        }
    }

    // Método para actualizar un schedule
    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                scheduleRepository.updateSchedule(schedule)
                loadSchedules()
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error updating schedule", e)
            }
        }
    }
}


// Data class para mostrar información completa del schedule
data class ScheduleWithDetails(
    val schedule: Schedule,
    val medication: Medication,
    val dosageUnit: DosageUnit,
    val weekDays: List<Int>,
    val nextDose: LocalDateTime?,
    val isCompletedToday: Boolean = false
)