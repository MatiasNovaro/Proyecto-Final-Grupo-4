package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule

interface ScheduleRepository {
    suspend fun insertSchedule(schedule: Schedule)
    suspend fun getSchedulesForMedication(medicationId: Long): List<Schedule>
    suspend fun getById(id: Int): Schedule?
    suspend fun deleteSchedule(schedule: Schedule)
}