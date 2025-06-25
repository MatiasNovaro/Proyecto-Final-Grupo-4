package ar.ort.edu.proyecto_final_grupo_4.domain.repository

import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationStatus
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduleWithMedication
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleWithDetails
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    suspend fun insertSchedule(schedule: Schedule): Long
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(scheduleId: Long)
    suspend fun getSchedulesForMedication(medicationId: Long): List<Schedule>
    suspend fun getAllSchedules(): List<Schedule>
    suspend fun getScheduleById(scheduleId: Long): Schedule?
    suspend fun getActiveSchedules(): List<Schedule>
    suspend fun getSchedulesForToday(): List<Schedule>
    suspend fun getSchedulesWithMedication(): List<ScheduleWithDetails>
    suspend fun deleteSchedulesForMedication(medicationId: Long)
    fun getSchedulesWithMedicationsByIds(scheduleIds: List<Long>): Flow<List<ScheduleWithMedication>>
    suspend fun updateScheduleStatus(scheduleId: Long, status: MedicationStatus)
    suspend fun deactivateSchedulesForMedication(medicationId: Long, newIsActive: Boolean)
}