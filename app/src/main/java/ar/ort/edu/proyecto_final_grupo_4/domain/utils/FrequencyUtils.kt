package ar.ort.edu.proyecto_final_grupo_4.domain.utils

import java.time.Duration
import java.time.LocalTime

data class FrequencyOption(
    val id: String,
    val displayName: String,
    val frequencyType: FrequencyType,
    val intervalValue: Int? = null,
    val description: String
)

// 5. Función para obtener frecuencias predefinidas
fun getCommonFrequencies(): List<FrequencyOption> {
    return listOf(
        // Diarias
        FrequencyOption(
            id = "daily_once",
            displayName = "Una vez al día",
            frequencyType = FrequencyType.DAILY,
            description = "Tomar una vez cada 24 horas"
        ),
        FrequencyOption(
            id = "daily_twice",
            displayName = "Dos veces al día",
            frequencyType = FrequencyType.TIMES_PER_DAY,
            intervalValue = 2,
            description = "Tomar cada 12 horas"
        ),
        FrequencyOption(
            id = "daily_three",
            displayName = "Tres veces al día",
            frequencyType = FrequencyType.TIMES_PER_DAY,
            intervalValue = 3,
            description = "Tomar cada 8 horas"
        ),
        FrequencyOption(
            id = "daily_four",
            displayName = "Cuatro veces al día",
            frequencyType = FrequencyType.TIMES_PER_DAY,
            intervalValue = 4,
            description = "Tomar cada 6 horas"
        ),

        // Por horas específicas
        FrequencyOption(
            id = "every_4h",
            displayName = "Cada 4 horas",
            frequencyType = FrequencyType.HOURS_INTERVAL,
            intervalValue = 4,
            description = "Tomar cada 4 horas"
        ),
        FrequencyOption(
            id = "every_6h",
            displayName = "Cada 6 horas",
            frequencyType = FrequencyType.HOURS_INTERVAL,
            intervalValue = 6,
            description = "Tomar cada 6 horas"
        ),
        FrequencyOption(
            id = "every_8h",
            displayName = "Cada 8 horas",
            frequencyType = FrequencyType.HOURS_INTERVAL,
            intervalValue = 8,
            description = "Tomar cada 8 horas"
        ),
        FrequencyOption(
            id = "every_12h",
            displayName = "Cada 12 horas",
            frequencyType = FrequencyType.HOURS_INTERVAL,
            intervalValue = 12,
            description = "Tomar cada 12 horas"
        ),

        // Semanales
        FrequencyOption(
            id = "weekly",
            displayName = "Una vez por semana",
            frequencyType = FrequencyType.WEEKLY,
            description = "Tomar el mismo día cada semana"
        ),

        // Cada varios días
        FrequencyOption(
            id = "every_2_days",
            displayName = "Cada 2 días",
            frequencyType = FrequencyType.DAYS_INTERVAL,
            intervalValue = 2,
            description = "Tomar un día sí, un día no"
        ),
        FrequencyOption(
            id = "every_3_days",
            displayName = "Cada 3 días",
            frequencyType = FrequencyType.DAYS_INTERVAL,
            intervalValue = 3,
            description = "Tomar cada 3 días"
        ),

        // Especiales
        FrequencyOption(
            id = "as_needed",
            displayName = "Cuando sea necesario",
            frequencyType = FrequencyType.AS_NEEDED,
            description = "Solo cuando se necesite (PRN)"
        )
    )
}

// 6. Función para generar horarios basados en la frecuencia
fun generateScheduleTimes(
    frequencyType: FrequencyType,
    intervalValue: Int?,
    startTime: LocalTime,
    endTime: LocalTime? = null
): List<LocalTime> {
    return when (frequencyType) {
        FrequencyType.DAILY -> listOf(startTime)

        FrequencyType.TIMES_PER_DAY -> {
            val times = intervalValue ?: 1
            val totalMinutes = if (endTime != null) {
                Duration.between(startTime, endTime).toMinutes()
            } else {
                12 * 60 // 12 horas por defecto
            }

            val intervalMinutes = totalMinutes / (times - 1)
            (0 until times).map { i ->
                startTime.plusMinutes((intervalMinutes * i).toLong())
            }
        }

        FrequencyType.HOURS_INTERVAL -> {
            val hours = intervalValue ?: 24
            val times = 24 / hours
            (0 until times).map { i ->
                startTime.plusHours((hours * i).toLong())
            }
        }

        FrequencyType.WEEKLY,
        FrequencyType.DAYS_INTERVAL -> listOf(startTime)

        FrequencyType.AS_NEEDED -> emptyList() // No horarios fijos
    }
}

// 7. Extensión para obtener los días de la semana para frecuencias semanales
fun getWeekDaysForFrequency(frequencyType: FrequencyType): List<Int>? {
    return when (frequencyType) {
        FrequencyType.DAILY -> (0..6).toList() // Todos los días
        FrequencyType.WEEKLY -> null // Usuario debe seleccionar
        else -> null
    }
}