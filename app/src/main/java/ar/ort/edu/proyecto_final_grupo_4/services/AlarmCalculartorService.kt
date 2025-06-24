package ar.ort.edu.proyecto_final_grupo_4.services

import ar.ort.edu.proyecto_final_grupo_4.domain.model.IndividualAlarmOccurrence
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import android.util.Log

class AlarmCalculatorService(
    val dosageUnitRepository: DosageUnitRepository
) {
    suspend fun calculateNextAlarms(schedule: Schedule, medication: Medication): List<IndividualAlarmOccurrence> {
        val alarms = mutableListOf<IndividualAlarmOccurrence>()
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        // Get relevant medication details from the `Medication` object
        val medName = medication.name
        val dosage = medication.dosage.toString() // Assuming dosage is a number
        val dosageUnit = dosageUnitRepository.getById(medication.dosageUnitID)

        when (schedule.frequencyType) {
            FrequencyType.DAILY -> {
                var currentDate = if (today >= schedule.startDate) today else schedule.startDate
                val endDate = schedule.endDate ?: currentDate.plusMonths(1)

                while (currentDate <= endDate) {
                    val alarmTime = LocalDateTime.of(currentDate, schedule.startTime)
                    if (alarmTime.isAfter(now.plusSeconds(5))) { // Add a small buffer to avoid immediate past triggers
                        // Generate a unique request code for each individual alarm time
                        val uniqueRequestCode = generateUniqueRequestCode(schedule.scheduleID, alarmTime)
                        if (dosageUnit != null) {
                            alarms.add(
                                IndividualAlarmOccurrence(
                                    scheduledTime = alarmTime,
                                    scheduleId = schedule.scheduleID, // Keep the original schedule ID for context
                                    medicationName = medName,
                                    dosage = dosage,
                                    dosageUnit = dosageUnit.name,
                                    uniqueRequestCode = uniqueRequestCode
                                )
                            )
                        }
                    }
                    currentDate = currentDate.plusDays(1)
                    if (alarms.size >= 50) break // Limit for performance/memory
                }
            }
            FrequencyType.HOURS_INTERVAL -> {
                schedule.intervalValue?.let { hours ->
                    if (hours <= 0) { // Avoid infinite loop or invalid interval
                        Log.e("AlarmCalculator", "Invalid interval value for HOURS_INTERVAL: $hours")
                        return emptyList()
                    }

                    // Calculate the start time of the first potential interval on the schedule's start date
                    val initialScheduleTime = LocalDateTime.of(schedule.startDate, schedule.startTime)

                    var currentPossibleAlarmTime: LocalDateTime

                    // If the schedule started today or in the past, calculate from now
                    if (schedule.startDate.isBefore(today) || schedule.startDate.isEqual(today)) {
                        // Find the first time slot TODAY that is after or at schedule.startTime
                        // and then find the *next* occurrence that is after 'now'
                        var baseTimeForToday = LocalDateTime.of(today, schedule.startTime)

                        // If baseTimeForToday is already in the past, advance it by intervals
                        while (baseTimeForToday.isBefore(now.minusHours(hours.toLong()))) { // Use now minus an interval to ensure we consider the *next* full interval
                            baseTimeForToday = baseTimeForToday.plusHours(hours.toLong())
                        }
                        currentPossibleAlarmTime = baseTimeForToday

                        // Ensure we don't schedule an alarm that's *already* too close or in the past
                        // If currentPossibleAlarmTime is still before now (e.g., if schedule.startTime was very early),
                        // find the next interval that is truly in the future.
                        while (currentPossibleAlarmTime.isBefore(now.plusSeconds(5))) { // Add buffer
                            currentPossibleAlarmTime = currentPossibleAlarmTime.plusHours(hours.toLong())
                        }

                    } else { // schedule.startDate is in the future
                        currentPossibleAlarmTime = LocalDateTime.of(schedule.startDate, schedule.startTime)
                    }

                    repeat(3) { // Limit to 3 future alarms for HOURS_INTERVAL
                        // Check if the currentPossibleAlarmTime is within the schedule's end date (if any)
                        if (schedule.endDate == null || currentPossibleAlarmTime.toLocalDate().isBefore(schedule.endDate.plusDays(1))) { // Check isBefore plusDays(1) for inclusive end date
                            // We already made sure it's after now.plusSeconds(5) above
                            val uniqueRequestCode = generateUniqueRequestCode(schedule.scheduleID, currentPossibleAlarmTime)
                            if (dosageUnit != null) {
                                alarms.add(
                                    IndividualAlarmOccurrence(
                                        scheduledTime = currentPossibleAlarmTime,
                                        scheduleId = schedule.scheduleID,
                                        medicationName = medName,
                                        dosage = dosage,
                                        dosageUnit = dosageUnit.name,
                                        uniqueRequestCode = uniqueRequestCode
                                    )
                                )
                            }
                            currentPossibleAlarmTime = currentPossibleAlarmTime.plusHours(hours.toLong()) // Move to the next interval
                        } else {
                            // If we've passed the end date, stop
                            return@repeat
                        }
                    }
                }
            }
            FrequencyType.TIMES_PER_DAY -> {
                schedule.intervalValue?.let { timesPerDay ->
                    if (timesPerDay <= 0) { // Avoid division by zero or infinite loop
                        Log.e("AlarmCalculator", "Invalid timesPerDay value: $timesPerDay")
                        return emptyList()
                    }

                    var currentDate = if (today.isBefore(schedule.startDate)) schedule.startDate else today // Start from schedule.startDate if it's in the future

                    val endDate = schedule.endDate ?: currentDate.plusMonths(1) // Default for open-ended schedules

                    while (currentDate.isBefore(endDate.plusDays(1)) && alarms.size < 50) { // Iterate until end date inclusive
                        val startTime = schedule.startTime
                        val endTime = schedule.endTime ?: LocalTime.of(22, 0)

                        val durationOfActivePeriod = Duration.between(startTime, endTime).toMinutes()
                        val intervalMinutes = if (timesPerDay > 1) {
                            durationOfActivePeriod / (timesPerDay - 1).toLong() // Divide by (timesPerDay - 1) for even intervals
                        } else {
                            0L
                        }

                        for (i in 0 until timesPerDay) {
                            val timeOfDay = if (timesPerDay == 1) {
                                startTime // If only once, it's at the start time
                            } else {
                                startTime.plusMinutes(intervalMinutes * i)
                            }

                            val alarmTimeCandidate = LocalDateTime.of(currentDate, timeOfDay)

                            // Only add if it's in the future (with a small buffer)
                            if (alarmTimeCandidate.isAfter(now.plusSeconds(5))) {
                                val uniqueRequestCode = generateUniqueRequestCode(schedule.scheduleID, alarmTimeCandidate)
                                if (dosageUnit != null) {
                                    alarms.add(
                                        IndividualAlarmOccurrence(
                                            scheduledTime = alarmTimeCandidate,
                                            scheduleId = schedule.scheduleID,
                                            medicationName = medName,
                                            dosage = dosage,
                                            dosageUnit = dosageUnit.name,
                                            uniqueRequestCode = uniqueRequestCode
                                        )
                                    )
                                }
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
                        if (alarmTime.isAfter(now.plusSeconds(5))) {
                            val uniqueRequestCode = generateUniqueRequestCode(schedule.scheduleID, alarmTime)
                            if (dosageUnit != null) {
                                alarms.add(
                                    IndividualAlarmOccurrence(
                                        scheduledTime = alarmTime,
                                        scheduleId = schedule.scheduleID,
                                        medicationName = medName,
                                        dosage = dosage,
                                        dosageUnit = dosageUnit.name,
                                        uniqueRequestCode = uniqueRequestCode
                                    )
                                )
                            }
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
                    if (alarmTime.isAfter(now.plusSeconds(5))) {
                        val uniqueRequestCode = generateUniqueRequestCode(schedule.scheduleID, alarmTime)
                        if (dosageUnit != null) {
                            alarms.add(
                                IndividualAlarmOccurrence(
                                    scheduledTime = alarmTime,
                                    scheduleId = schedule.scheduleID,
                                    medicationName = medName,
                                    dosage = dosage,
                                    dosageUnit = dosageUnit.name,
                                    uniqueRequestCode = uniqueRequestCode
                                )
                            )
                        }
                    }
                    currentDate = currentDate.plusWeeks(1)
                }
            }
            FrequencyType.AS_NEEDED -> TODO("Handle AS_NEEDED logic")
        }

        return alarms.sortedBy { it.scheduledTime }
    }

    // Helper function to generate a unique request code for each specific alarm occurrence
    private fun generateUniqueRequestCode(scheduleId: Long, alarmTime: LocalDateTime): Int {
        // Combine scheduleId and the alarm's specific time to create a unique hash
        return (scheduleId.toString() + alarmTime.toString()).hashCode()
    }
}