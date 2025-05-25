package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "medication_log",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["scheduleID"],
            childColumns = ["scheduleID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val logID: Int=0,
    val scheduleID: Int,
    val wasTaken: LocalDateTime
)
