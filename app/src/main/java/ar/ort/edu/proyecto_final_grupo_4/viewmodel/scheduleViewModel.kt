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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val dayOfWeekRepository: DayOfWeekRepository // Necesitarás crear este repositorio
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

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
    fun getTodaySchedules(): StateFlow<List<ScheduleWithDetails>> {
        // Implementar lógica para obtener schedules de hoy
        // considerando el día de la semana actual y las frecuencias
        return MutableStateFlow(emptyList()) // Placeholder
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
