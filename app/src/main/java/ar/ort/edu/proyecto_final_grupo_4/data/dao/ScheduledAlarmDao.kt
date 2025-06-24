package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduledAlarmRecord

@Dao
interface ScheduledAlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ScheduledAlarmRecord)

    @Query("SELECT * FROM scheduled_alarm_records WHERE scheduleId = :scheduleId")
    suspend fun getRecordsByScheduleId(scheduleId: Long): List<ScheduledAlarmRecord>

    @Query("SELECT * FROM scheduled_alarm_records WHERE medicationId = :medicationId")
    suspend fun getRecordsByMedicationId(medicationId: Long): List<ScheduledAlarmRecord>

    @Query("DELETE FROM scheduled_alarm_records WHERE requestCode = :requestCode")
    suspend fun deleteByRequestCode(requestCode: Int)

    @Query("DELETE FROM scheduled_alarm_records WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: Long)

    @Query("DELETE FROM scheduled_alarm_records WHERE medicationId = :medicationId")
    suspend fun deleteByMedicationId(medicationId: Long)

    @Query("DELETE FROM scheduled_alarm_records")
    suspend fun deleteAllRecords() // For full clear (e.g., app reset)
}