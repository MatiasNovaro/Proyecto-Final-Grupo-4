package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationStatus
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduleWithMedication
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    @Query("SELECT * FROM schedule WHERE medicationID = :medicationId")
    suspend fun getSchedulesForMedication(medicationId: Long): List<Schedule>

    @Query("SELECT * FROM schedule WHERE scheduleID = :id")
    suspend fun getById(id: Long): Schedule?

    @Query("DELETE FROM schedule WHERE scheduleID = :id")
    suspend fun deleteSchedule(id: Long)

    @Query("SELECT * FROM schedule")
    suspend fun getAllSchedules(): List<Schedule>

    @Query("DELETE FROM schedule WHERE medicationId = :medicationId")
    suspend fun deleteByMedicationId(medicationId: Long)

    @Query("SELECT * FROM schedule WHERE isActive = 1")
    suspend fun getActiveSchedules() : List<Schedule>

    @Query("SELECT * FROM schedule WHERE scheduleId IN (:scheduleIds)")
    fun getSchedulesWithMedicationsByIds(scheduleIds: List<Long>): Flow<List<ScheduleWithMedication>>

    @Query("UPDATE schedule SET status = :status WHERE scheduleId = :scheduleId")
    suspend fun updateScheduleStatus(scheduleId: Long, status: MedicationStatus)

    @Update
    suspend fun update(schedule: Schedule)

}
