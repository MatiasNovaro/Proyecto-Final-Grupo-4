package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.HistoryLogDisplayItem
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MedicationLogRepository {
    suspend fun insertLog(log: MedicationLog)
    suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog>
    suspend fun getAllLogs(): List<MedicationLog>
    suspend fun getMedicationLogs(lng: Long): List<MedicationLog>
    fun getAllMedicationLogs(): Flow<List<MedicationLog>>
    fun getMedicationLogsByStatus(wasTaken: Boolean): Flow<List<MedicationLog>>
    fun getMedicationLogsByDateRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<MedicationLog>>
    fun getDetailedMedicationLogs(): Flow<List<HistoryLogDisplayItem>>
    fun getDetailedMedicationLogsByStatus(wasTaken: Boolean): Flow<List<HistoryLogDisplayItem>>
    fun getDetailedMedicationLogsByDateRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<HistoryLogDisplayItem>>
    suspend fun getSpecificLogForScheduleAndExactTime(
        scheduleId: Long,
        exactScheduledTime: LocalDateTime // This remains LocalDateTime for convenience
    ): MedicationLog?
}