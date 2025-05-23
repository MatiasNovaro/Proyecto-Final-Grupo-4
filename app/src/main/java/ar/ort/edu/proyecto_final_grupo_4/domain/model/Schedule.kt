package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["medicationID"],
            childColumns = ["medicationID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Schedule(
    @PrimaryKey val scheduleID: Int,
    val medicationID: Int,
    val time: String // puede ser LocalTime si usás TypeConverter
)
