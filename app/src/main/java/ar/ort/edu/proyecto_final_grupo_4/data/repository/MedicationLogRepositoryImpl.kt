package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.MedicationLogDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.HistoryLogDisplayItem
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class MedicationLogRepositoryImpl @Inject constructor(
    private val medicationLogDao: MedicationLogDao
) : MedicationLogRepository {

    override suspend fun insertLog(log: MedicationLog) {
        require(log.scheduleID!! > 0) { "El ID de horario no es válido." }
        require(!log.timestamp.isAfter(LocalDateTime.now())) { "No se puede registrar una toma en el futuro." }
        medicationLogDao.insertLog(log)
    }

    override suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog> {
        return medicationLogDao.getLogsForSchedule(scheduleId)
    }

    override suspend fun getAllLogs(): List<MedicationLog> {
        return medicationLogDao.getAllLogs()
    }

    override suspend fun getMedicationLogs(medicationId: Long): List<MedicationLog> {
        return medicationLogDao.getMedicationLogs(medicationId)
    }
    override fun getAllMedicationLogs(): Flow<List<MedicationLog>> {
        return medicationLogDao.getAllMedicationLogs()
    }

    // Functions for future filtering:
    override fun getMedicationLogsByStatus(wasTaken: Boolean): Flow<List<MedicationLog>> {
        return medicationLogDao.getMedicationLogsByStatus(wasTaken)
    }

    override fun getMedicationLogsByDateRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<MedicationLog>> {
        return medicationLogDao.getMedicationLogsByDateRange(startTime, endTime)
    }

    override fun getDetailedMedicationLogs(): Flow<List<HistoryLogDisplayItem>> {
        return medicationLogDao.getDetailedMedicationLogs()
    }

    override fun getDetailedMedicationLogsByStatus(wasTaken: Boolean): Flow<List<HistoryLogDisplayItem>> {
        return medicationLogDao.getDetailedMedicationLogsByStatus(wasTaken)
    }

    override fun getDetailedMedicationLogsByDateRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<HistoryLogDisplayItem>> {
        return medicationLogDao.getDetailedMedicationLogsByDateRange(startTime, endTime)
    }


    override suspend fun getSpecificLogForScheduleAndExactTime(
        scheduleId: Long,
        exactScheduledTime: LocalDateTime
    ): MedicationLog? {
        val searchStartTime = exactScheduledTime.minusMinutes(15)
        val searchEndTime = exactScheduledTime.plusMinutes(60)

        return medicationLogDao.getLogForScheduleWithinTimeRange(
            scheduleId,
            searchStartTime,
            searchEndTime
        )
    }


}
