package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduleDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationStatus
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduleWithMedication
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override suspend fun insertSchedule(schedule: Schedule): Long {
        require(schedule.medicationID >= 0) { "Debe haber un medicamento asignado al horario." }
        return scheduleDao.insertSchedule(schedule)
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        TODO("Not yet implemented")
    }

    override suspend fun getSchedulesForMedication(medicationId: Long): List<Schedule> {
        return scheduleDao.getSchedulesForMedication(medicationId)
    }

    override suspend fun getAllSchedules(): List<Schedule> {
        return scheduleDao.getAllSchedules()
    }

    override suspend fun getScheduleById(scheduleId: Long): Schedule? {
        return scheduleDao.getById(scheduleId)
    }

    override suspend fun getActiveSchedules(): List<Schedule> {
       return  scheduleDao.getAllSchedules()
    }

    override suspend fun getSchedulesForToday(): List<Schedule> {
        TODO("Not yet implemented")
    }

    override suspend fun getSchedulesWithMedication(): List<ScheduleWithDetails> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSchedulesForMedication(medicationId: Long) {
        scheduleDao.deleteByMedicationId(medicationId)
    }

    override suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDao.deleteSchedule(scheduleId)
    }
    override fun getSchedulesWithMedicationsByIds(scheduleIds: List<Long>): Flow<List<ScheduleWithMedication>> {
        return scheduleDao.getSchedulesWithMedicationsByIds(scheduleIds)
    }

    override suspend fun updateScheduleStatus(scheduleId: Long, status: MedicationStatus) {
        scheduleDao.updateScheduleStatus(scheduleId, status)
    }
}

