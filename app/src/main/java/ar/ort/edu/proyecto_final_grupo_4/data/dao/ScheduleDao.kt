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
    suspend fun insertSchedule(schedule: Schedule)

    @Query("SELECT * FROM schedule WHERE medicationID = :medicationId")
    suspend fun getSchedulesForMedication(medicationId: Long): List<Schedule>

    @Query("SELECT * FROM schedule WHERE scheduleID = :id")
    suspend fun getById(id: Int): Schedule?

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)
}