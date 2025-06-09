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
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.history.ScheduleHistoryItem
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val dayOfWeekRepository: DayOfWeekRepository,
    private val medicationRepository: MedicationRepository,
    private val dosageUnitRepository: DosageUnitRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    private val _todaySchedules = MutableStateFlow<List<ScheduleWithDetails>>(emptyList())
    val todaySchedules: StateFlow<List<ScheduleWithDetails>> = _todaySchedules

    // NUEVO: StateFlow para el historial
    private val _historySchedules = MutableStateFlow<List<ScheduleHistoryItem>>(emptyList())
    val historySchedules: StateFlow<List<ScheduleHistoryItem>> = _historySchedules

    suspend fun getScheduleByMedicationId(medId: Long): Schedule? {
        return scheduleRepository.getScheduleById(medId)
    }

    // Método anterior para compatibilidad
    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.insertSchedule(schedule)
            loadSchedules() // Si tienes este método
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
            val dayOfWeek = today.dayOfWeek.value % 7 // Sunday = 0, Monday = 1, ...

            val todaySchedules = mutableListOf<ScheduleWithDetails>()

            for (schedule in allSchedules) {
                val isInDateRange = !schedule.startDate.isAfter(today) &&
                        (schedule.endDate == null || !today.isAfter(schedule.endDate))

                val weekDays: List<DayOfWeek> =
                    dayOfWeekRepository.getDaysForSchedule(schedule.scheduleID)
                val weekDayInts = weekDays.map { it.dayOfWeek }
                val isScheduledToday = when (schedule.frequencyType) {
                    FrequencyType.DAILY,
                    FrequencyType.HOURS_INTERVAL,
                    FrequencyType.TIMES_PER_DAY,
                    FrequencyType.DAYS_INTERVAL,
                    FrequencyType.AS_NEEDED -> true
                    FrequencyType.WEEKLY -> weekDayInts.contains(dayOfWeek)
                }

                if (isInDateRange && isScheduledToday) {
                    val med = medicationRepository.getById(schedule.medicationID)
                    val unit = med?.let { dosageUnitRepository.getById(it.dosageUnitID) }
                    val nextTime = schedule.startTime.atDate(today)
                    val isCompleted = false // implementar si querés tracking

                    if (med != null && unit != null) {
                        val detail = ScheduleWithDetails(
                            schedule = schedule,
                            medication = med,
                            dosageUnit = unit,
                            weekDays = weekDays,
                            nextDose = nextTime,
                            isCompletedToday = isCompleted
                        )
                        println(detail)
                        todaySchedules.add(detail)
                    }
                }
            }
            _todaySchedules.value = todaySchedules.sortedBy { it.nextDose }
        }
    }

    // NUEVO: Método para cargar el historial de medicamentos
    fun loadHistorySchedules() {
        viewModelScope.launch {
            try {
                val allSchedules = scheduleRepository.getAllSchedules()
                val historyItems = mutableListOf<ScheduleHistoryItem>()
                val today = LocalDate.now()

                // Generar historial para los últimos 7 días
                for (daysBack in 0..6) {
                    val targetDate = today.minusDays(daysBack.toLong())
                    val dayOfWeek = targetDate.dayOfWeek.value % 7

                    for (schedule in allSchedules) {
                        val isInDateRange = !schedule.startDate.isAfter(targetDate) &&
                                (schedule.endDate == null || !targetDate.isAfter(schedule.endDate))

                        val weekDays = dayOfWeekRepository.getDaysForSchedule(schedule.scheduleID)
                        val weekDayInts = weekDays.map { it.dayOfWeek }

                        val isScheduledThisDay = when (schedule.frequencyType) {
                            FrequencyType.DAILY,
                            FrequencyType.HOURS_INTERVAL,
                            FrequencyType.TIMES_PER_DAY,
                            FrequencyType.DAYS_INTERVAL,
                            FrequencyType.AS_NEEDED -> true
                            FrequencyType.WEEKLY -> weekDayInts.contains(dayOfWeek)
                        }

                        if (isInDateRange && isScheduledThisDay) {
                            val medication = medicationRepository.getById(schedule.medicationID)
                            val dosageUnit = medication?.let {
                                dosageUnitRepository.getById(it.dosageUnitID)
                            }

                            if (medication != null && dosageUnit != null) {
                                val historyItem = ScheduleHistoryItem(
                                    id = "${schedule.scheduleID}_${targetDate}",
                                    medicationName = medication.name,
                                    dosage = "${medication.dosage} ${dosageUnit.name}",
                                    time = schedule.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    isTaken = generateRandomTakenStatus(), // Por ahora random, después implementar tracking real
                                    dayLabel = getDayLabel(targetDate, today)
                                )
                                historyItems.add(historyItem)
                            }
                        }
                    }
                }

                _historySchedules.value = historyItems.sortedByDescending {
                    LocalDate.parse(it.dayLabel.takeLastWhile { it.isDigit() || it == '/' || it == '-' }.ifEmpty { "2024-01-01" })
                }.sortedBy { it.time }

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading history schedules", e)
                _historySchedules.value = emptyList()
            }
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
    val weekDays: List<DayOfWeek>,
    val nextDose: LocalDateTime?,
    val isCompletedToday: Boolean = false
)