package ar.ort.edu.proyecto_final_grupo_4.domain.utils

enum class FrequencyType {
    DAILY,           // Diariamente
    HOURS_INTERVAL,  // Cada X horas
    TIMES_PER_DAY,   // X veces al día
    WEEKLY,          // Semanalmente (días específicos)
    DAYS_INTERVAL,   // Cada X días
    AS_NEEDED        // Cuando sea necesario (PRN)
}