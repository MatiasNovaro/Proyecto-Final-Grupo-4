package ar.ort.edu.proyecto_final_grupo_4.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.ort.edu.proyecto_final_grupo_4.domain.model.HistoryLogDisplayItem
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MedicationLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog)

    @Query("SELECT * FROM medication_log WHERE scheduleID = :scheduleId")
    suspend fun getLogsForSchedule(scheduleId: Int): List<MedicationLog>

    @Query("SELECT * FROM medication_log ORDER BY wasTaken DESC")
    suspend fun getAllLogs(): List<MedicationLog>

    @Query("SELECT * FROM medication_log ORDER BY timestamp DESC") // Order by timestamp descending (most recent first)
    fun getAllMedicationLogs(): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_log WHERE wasTaken = :wasTaken ORDER BY timestamp DESC")
    fun getMedicationLogsByStatus(wasTaken: Boolean): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_log WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMedicationLogsByDateRange(startTime: java.time.LocalDateTime, endTime: java.time.LocalDateTime): Flow<List<MedicationLog>>

    @Query("""
        SELECT ml.* 
        FROM medication_log ml 
        INNER JOIN schedule s ON ml.scheduleID = s.scheduleID 
        WHERE s.medicationID = :medicationId
    """)
    suspend fun getMedicationLogs(medicationId: Long): List<MedicationLog>

    @Query("""
    SELECT
        ml.logID,
        ml.scheduleID,
        m.name AS medicationName,
        ml.dosageValue,   -- CHANGED: Select from medication_log
        ml.dosageUnit,    -- CHANGED: Select from medication_log
        ml.wasTaken,
        ml.timestamp
    FROM medication_log ml
    LEFT JOIN schedule s ON ml.scheduleID = s.scheduleId
    LEFT JOIN medication m ON s.medicationId = m.medicationId
    ORDER BY ml.timestamp DESC
""")
    fun getDetailedMedicationLogs(): Flow<List<HistoryLogDisplayItem>>

    // For status filtering
    @Query("""
    SELECT
        ml.logID,
        ml.scheduleID,
        m.name AS medicationName,
        ml.dosageValue,   -- CHANGED
        ml.dosageUnit,    -- CHANGED
        ml.wasTaken,
        ml.timestamp
    FROM medication_log ml
    LEFT JOIN schedule s ON ml.scheduleID = s.scheduleId
    LEFT JOIN medication m ON s.medicationId = m.medicationId
    WHERE ml.wasTaken = :wasTaken
    ORDER BY ml.timestamp DESC
""")
    fun getDetailedMedicationLogsByStatus(wasTaken: Boolean): Flow<List<HistoryLogDisplayItem>>

    @Query("""
    SELECT
        ml.logID,
        ml.scheduleID,
        m.name AS medicationName,
        ml.dosageValue,   -- CHANGED
        ml.dosageUnit,    -- CHANGED
        ml.wasTaken,
        ml.timestamp
    FROM medication_log ml
    LEFT JOIN schedule s ON ml.scheduleID = s.scheduleId
    LEFT JOIN medication m ON s.medicationId = m.medicationId
    WHERE ml.timestamp BETWEEN :startTime AND :endTime
    ORDER BY ml.timestamp DESC
""")
    fun getDetailedMedicationLogsByDateRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<HistoryLogDisplayItem>>

    @Query("""
        SELECT * FROM medication_log
        WHERE scheduleID = :scheduleId
          AND timestamp >= :exactScheduledTimeMillis - 60000 -- within 1 minute before
          AND timestamp <= :exactScheduledTimeMillis + 60000 -- within 1 minute after
        ORDER BY ABS(timestamp - :exactScheduledTimeMillis) -- Order by closest match
        LIMIT 1
    """)
    suspend fun getSpecificLogForScheduleAndExactTime(
        scheduleId: Long,
        exactScheduledTimeMillis: Long // Parameter is now Long (epoch milliseconds)
    ): MedicationLog?

    @Query("""
    SELECT * FROM medication_log
    WHERE scheduleID = :scheduleId
    AND timestamp BETWEEN :startTime AND :endTime
    LIMIT 1
""")
    suspend fun getLogForScheduleWithinTimeRange(
        scheduleId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): MedicationLog?
}