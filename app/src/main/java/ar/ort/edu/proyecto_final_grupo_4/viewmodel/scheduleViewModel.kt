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
import ar.ort.edu.proyecto_final_grupo_4.services.MedicationSchedulerService
import ar.ort.edu.proyecto_final_grupo_4.services.NotificationDismissalManager
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
    private val medicationlogRepository: MedicationLogRepository,
    private val notificationDismissalManager: NotificationDismissalManager,
    private val medicationSchedulerService: MedicationSchedulerService
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    private val _todaySchedules = MutableStateFlow<List<ScheduleWithDetails>>(emptyList())
    val todaySchedules: StateFlow<List<ScheduleWithDetails>> = _todaySchedules


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
            try {
                // Delegate to the service, which will handle fetching dosage and unit
                medicationSchedulerService.logMedicationTaken(scheduleId)

                // Dismiss the corresponding notification (if not handled by service)
                notificationDismissalManager.dismissMedicationNotification(scheduleId)
                scheduleRepository.updateScheduleStatus(scheduleId, MedicationStatus.TAKEN)
                Log.d("ScheduleViewModel", "Medication schedule $scheduleId marked as taken and notification dismissed.")

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error marking schedule $scheduleId as taken: ${e.message}", e)
            }
        }
    }

    fun markAsSkipped(scheduleId: Long) {
        viewModelScope.launch {
            try {
                // Delegate to the service, which will handle fetching dosage and unit
                medicationSchedulerService.logMedicationNotTaken(scheduleId)

                // Dismiss the corresponding notification (if not handled by service)
                notificationDismissalManager.dismissMedicationNotification(scheduleId)
                scheduleRepository.updateScheduleStatus(scheduleId, MedicationStatus.SKIPPED)
                Log.d("ScheduleViewModel", "Medication schedule $scheduleId marked as skipped and notification dismissed.")

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error marking schedule $scheduleId as skipped: ${e.message}", e)
            }
        }
    }
    fun snoozeAlarm(scheduleId: Long, minutes: Int) {
        viewModelScope.launch {
            try {
                // Delegate the snooze logic to MedicationSchedulerService
                medicationSchedulerService.snoozeAlarm(scheduleId, minutes)
                Log.d("ScheduleViewModel", "Snooze request for schedule $scheduleId by $minutes minutes delegated.")
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error delegating snooze for schedule $scheduleId: ${e.message}", e)
            }
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

    fun loadTodaySchedules() {
        viewModelScope.launch {
            val allSchedules = scheduleRepository.getAllSchedules()
            val today = LocalDate.now()
            val now = LocalDateTime.now() // Get current date and time

            val dayOfWeekJavaValue = today.dayOfWeek.value

            val currentDayOfWeekInt = if (dayOfWeekJavaValue == 7) 0 else dayOfWeekJavaValue

            val todaySchedules = mutableListOf<ScheduleWithDetails>()

            for (schedule in allSchedules) {

                if (!schedule.isActive) {
                    continue
                }
                val isInDateRange = !schedule.startDate.isAfter(today) &&
                        (schedule.endDate == null || !today.isAfter(schedule.endDate))

                if (!isInDateRange) continue

                val roomWeekDaysForSchedule: List<DayOfWeek> = dayOfWeekRepository.getDaysForSchedule(schedule.scheduleID)

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
                    val isPast = !isFutureDose && !isCurrentlyDue


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

    fun markScheduleAsCompleted(scheduleId: Int, date: LocalDate, time: LocalTime) {
        viewModelScope.launch {
            // Implementar lógica para marcar como completada
            // Probablemente necesites una tabla adicional para tracking de tomas
        }
    }

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