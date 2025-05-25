package ar.ort.edu.proyecto_final_grupo_4.viewmodel

import androidx.lifecycle.ViewModel
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DayOfWeekRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import ar.ort.edu.proyecto_final_grupo_4.domain.model.DayOfWeek
import kotlinx.coroutines.launch


@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val repeatDayRepository: DayOfWeekRepository
) : ViewModel() {

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    fun loadSchedules(medicationId: Int) {
        viewModelScope.launch {
            _schedules.value = scheduleRepository.getSchedulesForMedication(medicationId)
        }
    }

    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleRepository.insertSchedule(schedule)
            loadSchedules(schedule.medicationID)
        }
    }


    fun addRepeatDays(scheduleId: Int, days: List<Int>) {
        viewModelScope.launch {
            days.forEach { day ->
                repeatDayRepository.insertDayOfWeek(
                    DayOfWeek(scheduleID = scheduleId, dayOfWeek = day)
                )
            }
        }
    }
}
