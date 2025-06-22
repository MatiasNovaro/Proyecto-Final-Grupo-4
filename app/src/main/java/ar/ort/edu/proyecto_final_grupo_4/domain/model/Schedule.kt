package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import java.time.LocalDate
import java.time.LocalTime

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
    @PrimaryKey(autoGenerate = true)
    val scheduleID: Long = 0,
    val medicationID: Long,
    val frequencyType: FrequencyType,
    val intervalValue: Int? = null,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val isActive: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val status: MedicationStatus = MedicationStatus.PENDING
)