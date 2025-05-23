package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DayOfWeek

@Dao
interface DayOfWeekDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayOfWeek(day: DayOfWeek)

    @Query("SELECT * FROM day_of_week WHERE scheduleID = :scheduleId")
    suspend fun getDaysForSchedule(scheduleId: Int): List<DayOfWeek>

    @Query("DELETE FROM day_of_week WHERE scheduleID = :scheduleId")
    suspend fun deleteDaysForSchedule(scheduleId: Int)
}