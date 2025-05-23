package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationLogDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import javax.inject.Inject

class MedicationLogRepositoryImpl @Inject constructor(
    private val medicationLogDao: MedicationLogDao
) : MedicationLogRepository {

    override suspend fun insertLog(log: MedicationLog) {
        medicationLogDao.insertLog(log)
    }

    override suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog> {
        return medicationLogDao.getLogsForSchedule(scheduleId)
    }

    override suspend fun getAllLogs(): List<MedicationLog> {
        return medicationLogDao.getAllLogs()
    }
}
