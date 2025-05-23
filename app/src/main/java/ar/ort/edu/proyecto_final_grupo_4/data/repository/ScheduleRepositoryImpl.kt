package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduleDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule)
    }

    override suspend fun getSchedulesForMedication(medicationId: Int): List<Schedule> {
        return scheduleDao.getSchedulesForMedication(medicationId)
    }

    override suspend fun getById(id: Int): Schedule? {
        return scheduleDao.getById(id)
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule)
    }
}
