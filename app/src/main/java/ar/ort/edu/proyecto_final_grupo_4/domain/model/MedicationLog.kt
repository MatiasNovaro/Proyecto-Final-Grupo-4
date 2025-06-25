package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "medication_log",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["scheduleID"],
            childColumns = ["scheduleID"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val logID: Long=0,
    val scheduleID: Long,
    val wasTaken: Boolean,
    val timestamp: LocalDateTime
)
