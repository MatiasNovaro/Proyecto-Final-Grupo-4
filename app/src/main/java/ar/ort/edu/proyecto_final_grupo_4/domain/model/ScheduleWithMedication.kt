package ar.ort.edu.proyecto_final_grupo_4.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class ScheduleWithMedication(
    @Embedded val schedule: Schedule, // The Schedule object itself
    @Relation(
        parentColumn = "medicationID", // Column in Schedule
        entityColumn = "medicationID"        // Column in Medication
    )
    val medication: Medication           // The related Medication object
)
