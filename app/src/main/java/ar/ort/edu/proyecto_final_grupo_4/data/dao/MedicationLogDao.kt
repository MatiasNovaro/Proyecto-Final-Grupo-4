package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog

@Dao
interface MedicationLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog)

    @Query("SELECT * FROM medication_log WHERE scheduleID = :scheduleId")
    suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog>

    @Query("SELECT * FROM medication_log ORDER BY wasTaken DESC")
    suspend fun getAllLogs(): List<MedicationLog>


    @Query("""
        SELECT ml.* 
        FROM medication_log ml 
        INNER JOIN schedule s ON ml.scheduleID = s.scheduleID 
        WHERE s.medicationID = :medicationId
    """)
    suspend fun getMedicationLogs(medicationId: Long): List<MedicationLog>


}