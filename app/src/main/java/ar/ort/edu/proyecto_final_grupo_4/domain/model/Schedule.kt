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
    val intervalValue: Int? = null, // Para "cada X horas" o "cada X días" o "X veces al día"
    val startTime: LocalTime, // Primera toma del día
    val endTime: LocalTime? = null, // Para rango de horas (opcional)
    val isActive: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,// Para tratamientos con fecha fin
    val status: MedicationStatus = MedicationStatus.PENDING
)