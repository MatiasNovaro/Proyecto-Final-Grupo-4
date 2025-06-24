package ar.ort.edu.proyecto_final_grupo_4.domain.model

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZoneId

data class MedicationAlarm(
    val scheduleId: Long,
    val medicationName: String,
    val dosage: String,
    val dosageUnit: String,
    var scheduledTime: LocalDateTime,
    val requestCode: Int = generateRequestCode(scheduleId, scheduledTime)

) {
    companion object {
        private fun generateRequestCode(scheduleId: Long, scheduledTime: LocalDateTime): Int {
            return "${scheduleId}_${scheduledTime.toEpochSecond(ZoneOffset.UTC)}".hashCode()
        }
    }
}
