package ar.ort.edu.proyecto_final_grupo_4.domain.model

import java.time.LocalDateTime

data class MedicationAlarm(
    val scheduleId: Long,
    val medicationName: String,
    val dosage: String,
    val dosageUnit: String,
    val scheduledTime: LocalDateTime,
    val requestCode: Int = scheduleId.toInt()

)
