package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleWithDetails

interface ScheduleRepository {
    suspend fun insertSchedule(schedule: Schedule): Long
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(scheduleId: Long)
    suspend fun getSchedulesForMedication(medicationId: Long): List<Schedule>
    suspend fun getAllSchedules(): List<Schedule>
    suspend fun getScheduleById(scheduleId: Long): Schedule?


    // Métodos adicionales que podrías necesitar
    suspend fun getActiveSchedules(): List<Schedule>
    suspend fun getSchedulesForToday(): List<Schedule>
    suspend fun getSchedulesWithMedication(): List<ScheduleWithDetails>
    suspend fun deleteSchedulesForMedication(medicationId: Long)
}