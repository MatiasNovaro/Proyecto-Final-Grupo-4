package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.DayOfWeekDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DayOfWeek
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import javax.inject.Inject

class DayOfWeekRepositoryImpl @Inject constructor(
    private val dayOfWeekDao: DayOfWeekDao
) : DayOfWeekRepository {

    override suspend fun insertDayOfWeek(day: DayOfWeek) {
        // Validar que el scheduleID sea válido
        require(day.scheduleID > 0) { "El horario (scheduleID) no es válido." }

        // Validar que no haya duplicados
        val existingDays = dayOfWeekDao.getDaysForSchedule(day.scheduleID)
        val duplicateDay = existingDays.any { it.dayOfWeek == day.dayOfWeek }
        require(!duplicateDay) { "El día seleccionado ya está asignado a este horario." }

        dayOfWeekDao.insertDayOfWeek(day)
    }

    override suspend fun getDaysForSchedule(scheduleId: Long): List<DayOfWeek> {
        return dayOfWeekDao.getDaysForSchedule(scheduleId)
    }

    override suspend fun deleteDaysForSchedule(scheduleId: Long) {
        dayOfWeekDao.deleteDaysForSchedule(scheduleId)
    }
}
