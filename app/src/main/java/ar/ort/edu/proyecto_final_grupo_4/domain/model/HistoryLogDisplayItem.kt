package ar.ort.edu.proyecto_final_grupo_4.domain.model

import java.time.LocalDateTime

data class HistoryLogDisplayItem(
    val logID: Long,
    val scheduleID: Long?, // Should be nullable if scheduleID in MedicationLog is nullable
    val medicationName: String?, // Still from 'm.name', keep nullable as LEFT JOIN is preferred
    val dosageValue: String, // CHANGE: Use this from ml.dosageValue
    val dosageUnit: String,  // CHANGE: Use this from ml.dosageUnit
    val wasTaken: Boolean,
    val timestamp: LocalDateTime,
)
