package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationLogDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import java.time.LocalDateTime
import javax.inject.Inject

class MedicationLogRepositoryImpl @Inject constructor(
    private val medicationLogDao: MedicationLogDao
) : MedicationLogRepository {

    override suspend fun insertLog(log: MedicationLog) {
        require(log.scheduleID > 0) { "El ID de horario no es válido." }
        require(!log.wasTaken.isAfter(LocalDateTime.now())) { "No se puede registrar una toma en el futuro." }
        medicationLogDao.insertLog(log)
    }

    override suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog> {
        return medicationLogDao.getLogsForSchedule(scheduleId)
    }

    override suspend fun getAllLogs(): List<MedicationLog> {
        return medicationLogDao.getAllLogs()
    }
}
