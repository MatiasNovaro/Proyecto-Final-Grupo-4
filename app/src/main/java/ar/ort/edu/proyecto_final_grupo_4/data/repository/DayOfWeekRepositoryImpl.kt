package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.DayOfWeekDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DayOfWeek
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import javax.inject.Inject

class DayOfWeekRepositoryImpl @Inject constructor(
    private val dayOfWeekDao: DayOfWeekDao
) : DayOfWeekRepository {

    override suspend fun insertDayOfWeek(day: DayOfWeek) {
        dayOfWeekDao.insertDayOfWeek(day)
    }

    override suspend fun getDaysForSchedule(scheduleId: Int): List<DayOfWeek> {
        return dayOfWeekDao.getDaysForSchedule(scheduleId)
    }

    override suspend fun deleteDaysForSchedule(scheduleId: Int) {
        dayOfWeekDao.deleteDaysForSchedule(scheduleId)
    }
}