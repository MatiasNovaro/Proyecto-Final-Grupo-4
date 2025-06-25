package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "scheduled_alarm_records")
data class ScheduledAlarmRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Unique ID for this record
    val scheduleId: Long, // ID of the parent medication schedule
    val medicationId: Long, // ID of the medication (useful for bulk cancellations)
    val requestCode: Int, // The unique request code used to set the PendingIntent
    val scheduledTime: LocalDateTime,
    val medicationName: String, // Store this extra
    val dosage: String,         // Store this extra
    val dosageUnit: String,     // Store this extra
    val isSnoozeAlarm: Boolean = false // Store this extra
)
