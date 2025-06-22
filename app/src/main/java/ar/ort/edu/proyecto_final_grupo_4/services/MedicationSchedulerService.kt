package ar.ort.edu.proyecto_final_grupo_4.services

import android.content.Context
import android.util.Log
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationAlarm
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime

class MedicationSchedulerService(
    private val scheduleRepository: ScheduleRepository,
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val dosageUnitRepository: DosageUnitRepository,
    private val alarmCalculator: AlarmCalculartorService,
    private val alarmManager: MedicationAlarmManager,
    @ApplicationContext private val context: Context
) {
    suspend fun scheduleAllActiveMedications() {
        try {
            val activeSchedules = scheduleRepository.getActiveSchedules()

            activeSchedules.forEach { schedule ->
                val medication = medicationRepository.getById(schedule.medicationID)
                medication?.let { med ->
                    val dosageUnit = dosageUnitRepository.getById(med.dosageUnitID)
                    scheduleAlarmsForMedication(schedule, med, dosageUnit?.name ?: "")
                }
            }
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error programando medicamentos", e)
        }
    }

    private fun scheduleAlarmsForMedication(
        schedule: Schedule,
        medication: Medication,
        dosageUnitName: String
    ) {
        val nextAlarms = alarmCalculator.calculateNextAlarms(schedule, medication)

        val medicationAlarms = nextAlarms.map { alarmTime ->
            MedicationAlarm(
                scheduleId = schedule.scheduleID,
                medicationName = medication.name,
                dosage = medication.dosage,
                dosageUnit = dosageUnitName,
                scheduledTime = alarmTime
            )
        }

        alarmManager.scheduleAllAlarms(medicationAlarms)
    }

    suspend fun rescheduleMedication(scheduleId: Long) {
        try {
            val schedule = scheduleRepository.getScheduleById(scheduleId)
            schedule?.let { sched ->
                alarmManager.cancelAlarm(sched.scheduleID)

                if (sched.isActive) {
                    val medication = medicationRepository.getById(sched.medicationID)
                    medication?.let { med ->
                        val dosageUnit = dosageUnitRepository.getById(med.dosageUnitID)
                        // Then, reschedule new ones based on the updated schedule
                        scheduleAlarmsForMedication(sched, med, dosageUnit?.name ?: "")
                        println("------Alarma reprogramada para schedule ID: ${sched.scheduleID}-----")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error reprogramando medicamento", e)
        }
    }
    suspend fun cancelAlarmsForMedication(medicationId: Long) {
        try {
            val schedulesForMed = scheduleRepository.getSchedulesForMedication(medicationId)
            schedulesForMed.forEach { schedule ->
                alarmManager.cancelAlarm(schedule.scheduleID) // This needs to cancel all PIs associated with this scheduleID
            }
            Log.d("MedicationSchedulerService", "Cancelled alarms for medication ID: $medicationId")
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error cancelling alarms for medication", e)
        }
    }

//    suspend fun cancelMedicationAlarms(scheduleId: Long) {
//        alarmManager.cancelAlarm(scheduleId)
//    }

    suspend fun logMedicationTaken(scheduleId: Long) {
        try {
            val medicationLog = MedicationLog(
                scheduleID = scheduleId,
                wasTaken = true,
                timestamp = LocalDateTime.now()
            )
            medicationLogRepository.insertLog(medicationLog)
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error guardando log de medicamento", e)
        }
    }
    suspend fun logMedicationNotTaken(scheduleId: Long) {
        try {
            val medicationLog = MedicationLog(
                scheduleID = scheduleId,
                wasTaken = false,
                timestamp = LocalDateTime.now()
            )
            medicationLogRepository.insertLog(medicationLog)
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error guardando log de medicamento", e)
        }
    }
}
