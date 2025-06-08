package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog

interface MedicationLogRepository {
    suspend fun insertLog(log: MedicationLog)
    suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog>
    suspend fun getAllLogs(): List<MedicationLog>
    suspend fun getMedicationLogs(lng: Long): List<MedicationLog>
}