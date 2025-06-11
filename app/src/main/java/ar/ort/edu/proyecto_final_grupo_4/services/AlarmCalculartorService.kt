package ar.ort.edu.proyecto_final_grupo_4.services

import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmCalculartorService {
    fun calculateNextAlarms(schedule: Schedule, medication: Medication): List<LocalDateTime> {
        val alarms = mutableListOf<LocalDateTime>()
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        when (schedule.frequencyType) {
            FrequencyType.DAILY -> {
                var currentDate = if (today >= schedule.startDate) today else schedule.startDate
                val endDate = schedule.endDate ?: currentDate.plusMonths(1)

                while (currentDate <= endDate) {
                    val alarmTime = LocalDateTime.of(currentDate, schedule.startTime)
                    if (alarmTime.isAfter(now)) {
                        alarms.add(alarmTime)
                    }
                    currentDate = currentDate.plusDays(1)

                    // Limitar a próximas 50 alarmas para evitar sobrecarga
                    if (alarms.size >= 50) break
                }
            }

            FrequencyType.HOURS_INTERVAL -> {
                schedule.intervalValue?.let { hours ->
                    var nextAlarm = LocalDateTime.of(today, schedule.startTime)
                    if (nextAlarm.isBefore(now)) {
                        nextAlarm = nextAlarm.plusHours(((now.hour - schedule.startTime.hour) / hours + 1) * hours.toLong())
                    }

                    repeat(50) { // Próximas 50 alarmas
                        if (schedule.endDate == null || nextAlarm.toLocalDate() <= schedule.endDate) {
                            alarms.add(nextAlarm)
                            nextAlarm = nextAlarm.plusHours(hours.toLong())
                        }
                    }
                }
            }

            FrequencyType.TIMES_PER_DAY -> {
                schedule.intervalValue?.let { timesPerDay ->
                    var currentDate = if (today >= schedule.startDate) today else schedule.startDate
                    val endDate = schedule.endDate ?: currentDate.plusMonths(1)

                    while (currentDate <= endDate && alarms.size < 50) {
                        val startTime = schedule.startTime
                        val endTime = schedule.endTime ?: LocalTime.of(22, 0)
                        val intervalMinutes = Duration.between(startTime, endTime).toMinutes() / timesPerDay

                        repeat(timesPerDay) { index ->
                            val alarmTime = LocalDateTime.of(
                                currentDate,
                                startTime.plusMinutes(intervalMinutes * index)
                            )
                            if (alarmTime.isAfter(now)) {
                                alarms.add(alarmTime)
                            }
                        }
                        currentDate = currentDate.plusDays(1)
                    }
                }
            }

            FrequencyType.DAYS_INTERVAL -> {
                schedule.intervalValue?.let { days ->
                    var currentDate = if (today >= schedule.startDate) today else schedule.startDate
                    val endDate = schedule.endDate ?: currentDate.plusMonths(3)

                    while (currentDate <= endDate && alarms.size < 50) {
                        val alarmTime = LocalDateTime.of(currentDate, schedule.startTime)
                        if (alarmTime.isAfter(now)) {
                            alarms.add(alarmTime)
                        }
                        currentDate = currentDate.plusDays(days.toLong())
                    }
                }
            }

            FrequencyType.WEEKLY -> {
                var currentDate = if (today >= schedule.startDate) today else schedule.startDate
                val endDate = schedule.endDate ?: currentDate.plusMonths(3)

                while (currentDate <= endDate && alarms.size < 50) {
                    val alarmTime = LocalDateTime.of(currentDate, schedule.startTime)
                    if (alarmTime.isAfter(now)) {
                        alarms.add(alarmTime)
                    }
                    currentDate = currentDate.plusWeeks(1)
                }
            }

            FrequencyType.AS_NEEDED -> TODO()
        }

        return alarms.sortedBy { it }
    }
}