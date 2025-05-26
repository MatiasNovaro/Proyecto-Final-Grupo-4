package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.DayOfWeek

interface DayOfWeekRepository {
    suspend fun insertDayOfWeek(day: DayOfWeek)
    suspend fun getDaysForSchedule(scheduleId: Long): List<DayOfWeek>
    suspend fun deleteDaysForSchedule(scheduleId: Long)
}