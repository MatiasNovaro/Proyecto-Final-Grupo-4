package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule

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

}
