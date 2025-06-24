package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduledAlarmRecord

interface ScheduledAlarmRepository {
    suspend fun insertScheduledAlarmRecord(record: ScheduledAlarmRecord)
    suspend fun getScheduledAlarmRecordsByScheduleId(scheduleId: Long): List<ScheduledAlarmRecord>
    suspend fun getScheduledAlarmRecordsByMedicationId(medicationId: Long): List<ScheduledAlarmRecord>
    suspend fun deleteScheduledAlarmRecordByRequestCode(requestCode: Int)
    suspend fun deleteScheduledAlarmRecordsByScheduleId(scheduleId: Long)
    suspend fun deleteScheduledAlarmRecordsByMedicationId(medicationId: Long)
    suspend fun deleteAllScheduledAlarmRecords()
}