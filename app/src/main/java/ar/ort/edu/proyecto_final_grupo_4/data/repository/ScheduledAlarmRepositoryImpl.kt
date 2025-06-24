package ar.ort.edu.proyecto_final_grupo_4.data.repository

import ar.ort.edu.proyecto_final_grupo_4.data.dao.ScheduledAlarmDao
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduledAlarmRecord
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduledAlarmRepository
import javax.inject.Inject

class ScheduledAlarmRepositoryImpl @Inject constructor(
    private val scheduledAlarmDao: ScheduledAlarmDao
) : ScheduledAlarmRepository {
    override suspend fun insertScheduledAlarmRecord(record: ScheduledAlarmRecord) {
        scheduledAlarmDao.insert(record)
    }

    override suspend fun getScheduledAlarmRecordsByScheduleId(scheduleId: Long): List<ScheduledAlarmRecord> {
        return scheduledAlarmDao.getRecordsByScheduleId(scheduleId)
    }

    override suspend fun getScheduledAlarmRecordsByMedicationId(medicationId: Long): List<ScheduledAlarmRecord> {
        return scheduledAlarmDao.getRecordsByMedicationId(medicationId)
    }

    override suspend fun deleteScheduledAlarmRecordByRequestCode(requestCode: Int) {
        scheduledAlarmDao.deleteByRequestCode(requestCode)
    }

    override suspend fun deleteScheduledAlarmRecordsByScheduleId(scheduleId: Long) {
        scheduledAlarmDao.deleteByScheduleId(scheduleId)
    }

    override suspend fun deleteScheduledAlarmRecordsByMedicationId(medicationId: Long) {
        scheduledAlarmDao.deleteByMedicationId(medicationId)
    }

    override suspend fun deleteAllScheduledAlarmRecords() {
        scheduledAlarmDao.deleteAllRecords()
    }
}