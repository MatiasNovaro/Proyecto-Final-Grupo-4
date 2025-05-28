package ar.ort.edu.proyecto_final_grupo_4.viewmodel.functions

import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.utils.FrequencyType
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleWithDetails
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime


//fun loadTodaySchedules() {
//    viewModelScope.launch {
//        val allSchedules = scheduleRepository.getAllSchedules()
//        val today = LocalDate.now()
//        val dayOfWeek = today.dayOfWeek.value % 7
//
//        val todaySchedules = mutableListOf<ScheduleWithDetails>()
//
//        for (schedule in allSchedules) {
//            val isInDateRange = !schedule.startDate.isAfter(today) &&
//                    (schedule.endDate == null || !today.isAfter(schedule.endDate))
//
//            val weekDays = dayOfWeekRepository.getDaysForSchedule(schedule.scheduleID)
//            val weekDayInts = weekDays.map { it.dayOfWeek }
//            val isScheduledToday = when (schedule.frequencyType) {
//                FrequencyType.DAILY,
//                FrequencyType.HOURS_INTERVAL,
//                FrequencyType.TIMES_PER_DAY,
//                FrequencyType.DAYS_INTERVAL,
//                FrequencyType.AS_NEEDED -> true
//
//                FrequencyType.WEEKLY -> weekDayInts.contains(dayOfWeek)
//            }
//
//            if (isInDateRange && isScheduledToday) {
//                val med = medicationRepository.getById(schedule.medicationID)
//                val unit = med?.let { dosageUnitRepository.getById(it.dosageUnitID) }
//                val nextTime = schedule.startTime.atDate(today)
//                val now = LocalDateTime.now()
//                val isCompleted = false
//
//                if (med != null && unit != null && nextTime.isAfter(now)) {
//                    todaySchedules.add(
//                        ScheduleWithDetails(
//                            schedule = schedule,
//                            medication = med,
//                            dosageUnit = unit,
//                            weekDays = weekDays,
//                            nextDose = nextTime,
//                            isCompletedToday = isCompleted
//                        )
//                    )
//                }
//            }
//        }
//
//        _todaySchedules.value = todaySchedules.sortedBy { it.nextDose }
//    }
//}
//
