package ar.ort.edu.proyecto_final_grupo_4.domain.model

import java.time.LocalDateTime

data class HistoryYUiItem(
    val logID: Long,
    val scheduleID: Long,
    val medicationName: String,
    val dosage: String,
    val wasTaken: Boolean,
    val timestamp: LocalDateTime,
    val dayLabel: String // This is for the UI
)
