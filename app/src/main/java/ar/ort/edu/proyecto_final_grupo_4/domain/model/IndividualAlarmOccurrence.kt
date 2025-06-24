package ar.ort.edu.proyecto_final_grupo_4.domain.model

import java.time.LocalDateTime

data class IndividualAlarmOccurrence(
    val scheduledTime: LocalDateTime,
    val scheduleId: Long, // Original schedule ID
    val medicationName: String,
    val dosage: String,
    val dosageUnit: String,
    val uniqueRequestCode: Int
)
