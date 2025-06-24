package ar.ort.edu.proyecto_final_grupo_4.domain.utils

enum class FrequencyType(val displayName: String, val description: String) {
    DAILY("Diariamente", "Toma el medicamento cada día a la misma hora."),
    HOURS_INTERVAL("Cada X horas", "Toma el medicamento cada cierto número de horas."),
    TIMES_PER_DAY("X veces al día", "Toma el medicamento un número específico de veces al día."),
    DAYS_INTERVAL("Cada X días", "Toma el medicamento cada cierto número de días."),
    WEEKLY("Semanalmente", "Toma el medicamento ciertos días de la semana."),
    AS_NEEDED("Según sea necesario", "Toma el medicamento solo cuando lo necesites.");
}