package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "day_of_week",
    primaryKeys = ["scheduleID", "dayOfWeek"],
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["scheduleID"],
            childColumns = ["scheduleID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DayOfWeek(
    val scheduleID: Int,
    val dayOfWeek: Int // 0 = Sunday, 6 = Saturday
)
