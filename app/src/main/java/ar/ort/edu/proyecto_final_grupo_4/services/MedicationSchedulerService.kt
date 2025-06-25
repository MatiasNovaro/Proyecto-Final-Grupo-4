package ar.ort.edu.proyecto_final_grupo_4.services

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import ar.ort.edu.proyecto_final_grupo_4.domain.model.IndividualAlarmOccurrence
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Medication
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationAlarm
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationLog
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduledAlarmRecord
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.DosageUnitRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationLogRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduledAlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject

class MedicationSchedulerService @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val medicationRepository: MedicationRepository,
    private val medicationLogRepository: MedicationLogRepository,
    private val dosageUnitRepository: DosageUnitRepository,
    private val alarmCalculator: AlarmCalculatorService, // Note: Confirmed 'AlarmCalculatorService' spelling.
    private val alarmManager: MedicationAlarmManager,
    private val notificationDismissalManager: NotificationDismissalManager,
    private val scheduledAlarmRepository: ScheduledAlarmRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun scheduleAllActiveMedications() {
        try {
            val activeSchedules = scheduleRepository.getActiveSchedules()

            activeSchedules.forEach { schedule ->
                val medication = medicationRepository.getById(schedule.medicationID)
                medication?.let { med ->
                    // dosageUnitName is now inside IndividualAlarmOccurrence, so it's not strictly needed here
                    // val dosageUnit = dosageUnitRepository.getById(med.dosageUnitID)
                    scheduleAlarmsForMedication(schedule, med) // Removed dosageUnitName parameter
                }
            }
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error programando medicamentos", e)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private suspend fun scheduleAlarmsForMedication(
        schedule: Schedule,
        medication: Medication
    ) {
        val nextAlarms: List<IndividualAlarmOccurrence> = alarmCalculator.calculateNextAlarms(schedule, medication)

        // Convert IndividualAlarmOccurrence to MedicationAlarm for the AlarmManager
        val medicationAlarmsToSchedule = nextAlarms.map { individualAlarm ->
            MedicationAlarm(
                scheduleId = individualAlarm.scheduleId,
                medicationName = individualAlarm.medicationName,
                dosage = individualAlarm.dosage,
                dosageUnit = individualAlarm.dosageUnit,
                scheduledTime = individualAlarm.scheduledTime,
                requestCode = individualAlarm.uniqueRequestCode
            )
        }

        // Schedule and save each alarm record
        medicationAlarmsToSchedule.forEach { alarmToSchedule ->
            alarmManager.scheduleAlarm(alarmToSchedule)

            // Save the details of the *successfully scheduled* alarm
            val record = ScheduledAlarmRecord(
                scheduleId = alarmToSchedule.scheduleId,
                medicationId = medication.medicationID, // Make sure Medication has an ID property
                requestCode = alarmToSchedule.requestCode,
                scheduledTime = alarmToSchedule.scheduledTime
            )
            scheduledAlarmRepository.insertScheduledAlarmRecord(record)
            Log.d("MedSchedulerService", "Scheduled alarm and saved record: ${record.requestCode} for ${record.scheduledTime}")
        }
        Log.d("MedSchedulerService", "Finished scheduling ${medicationAlarmsToSchedule.size} alarms for schedule ID: ${schedule.scheduleID}")
    }

    suspend fun rescheduleMedication(scheduleId: Long) {
        try {
            val schedule = scheduleRepository.getScheduleById(scheduleId)
            schedule?.let { sched ->
                val medication = medicationRepository.getById(sched.medicationID)
                medication?.let { med ->
                    // 1. Cancel ALL currently scheduled alarms specifically for this schedule
                    cancelAlarmsForSchedule(sched.scheduleID)

                    // 2. If the schedule is still active, calculate and schedule NEW alarms
                    if (sched.isActive) {
                        scheduleAlarmsForMedication(sched, med)
                        Log.d("MedSchedulerService", "Alarms re-scheduled for schedule ID: ${sched.scheduleID}")
                    } else {
                        // If schedule became inactive, just cancel and don't re-schedule
                        Log.d("MedSchedulerService", "Schedule ID: ${sched.scheduleID} is inactive, alarms cancelled.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MedSchedulerService", "Error rescheduling medication for schedule ID: $scheduleId", e)
        }
    }

    // This method needs to be updated to retrieve all request codes for a medication's schedules
    // and then call alarmManager.cancelAlarm(requestCode) for each.
    private suspend fun cancelAlarmsForSchedule(scheduleId: Long) {
        try {
            val scheduledRecords = scheduledAlarmRepository.getScheduledAlarmRecordsByScheduleId(scheduleId)
            scheduledRecords.forEach { record ->
                alarmManager.cancelAlarm(record.requestCode)
                scheduledAlarmRepository.deleteScheduledAlarmRecordByRequestCode(record.requestCode) // Delete from DB
                Log.d("MedSchedulerService", "Cancelled and removed record for requestCode: ${record.requestCode} (Schedule ID: ${record.scheduleId})")
            }
            Log.d("MedSchedulerService", "Finished cancelling and clearing records for schedule ID: $scheduleId")
        } catch (e: Exception) {
            Log.e("MedSchedulerService", "Error cancelling alarms for schedule ID: $scheduleId", e)
        }
    }

    // Modify existing cancelAlarmsForMedication to use the new repository
    suspend fun cancelAlarmsForMedication(medicationId: Long) {
        try {
            val scheduledRecords = scheduledAlarmRepository.getScheduledAlarmRecordsByMedicationId(medicationId)
            scheduledRecords.forEach { record ->
                alarmManager.cancelAlarm(record.requestCode)
                scheduledAlarmRepository.deleteScheduledAlarmRecordByRequestCode(record.requestCode) // Delete from DB
                Log.d("MedSchedulerService", "Cancelled and removed record for requestCode: ${record.requestCode} (Med ID: ${record.medicationId})")
            }
            Log.d("MedSchedulerService", "Finished cancelling and clearing records for medication ID: $medicationId")
        } catch (e: Exception) {
            Log.e("MedSchedulerService", "Error cancelling alarms for medication ID: $medicationId", e)
        }
    }

    suspend fun logMedicationTaken(scheduleId: Long) {
        try {
            val medicationLog = MedicationLog(
                scheduleID = scheduleId,
                wasTaken = true,
                timestamp = LocalDateTime.now()
            )
            medicationLogRepository.insertLog(medicationLog)
            // Dismiss the notification. Needs NotificationDismissalManager injected.
            // notificationDismissalManager.dismissMedicationNotification(scheduleId)
            Log.d("MedicationSchedulerService", "Log for schedule $scheduleId saved as TAKEN.")
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error saving log for taken medication", e)
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
            // Dismiss the notification. Needs NotificationDismissalManager injected.
            // notificationDismissalManager.dismissMedicationNotification(scheduleId)
            Log.d("MedicationSchedulerService", "Log for schedule $scheduleId saved as NOT TAKEN.")
        } catch (e: Exception) {
            Log.e("MedicationSchedulerService", "Error saving log for not taken medication", e)
        }
    }
    suspend fun snoozeAlarm(scheduleId: Long, minutes: Int) {
        try {
            Log.d("MedSchedulerService", "Attempting to create snooze alarm for schedule ID: $scheduleId for $minutes minutes from now.")

            // 1. Dismiss the original notification immediately.
            notificationDismissalManager.dismissMedicationNotification(scheduleId)
            Log.d("MedSchedulerService", "Original notification for schedule ID: $scheduleId dismissed.")

            // 2. Calculate the new snooze time based on *current time*.
            val newSnoozeTime = LocalDateTime.now().plusMinutes(minutes.toLong())
            Log.d("MedSchedulerService", "Snooze alarm will trigger at: $newSnoozeTime")

            // 3. Retrieve necessary medication details for the temporary alarm payload.
            val schedule = scheduleRepository.getScheduleById(scheduleId)
            if (schedule == null) {
                Log.e("MedSchedulerService", "Schedule not found for ID: $scheduleId. Cannot create snooze alarm.")
                return
            }
            val medication = medicationRepository.getById(schedule.medicationID)
            if (medication == null) {
                Log.e("MedSchedulerService", "Medication not found for schedule ID: ${schedule.scheduleID}. Cannot create snooze alarm.")
                return
            }
            val dosageUnit = dosageUnitRepository.getById(medication.dosageUnitID)

            // 4. Generate a *unique* request code for this temporary snooze alarm.
            //    It MUST be different from the main schedule's request codes.
            //    A simple way is to use a high offset or combine with current time/scheduleId.
            //    Using scheduleId * 1000000 + (a time-based component) or similar.
            //    Using a specific range for snooze alarms can also help.
            //    For now, let's ensure it's different.
            //    A common pattern: Use a very large number + scheduleId to prevent collisions with regular alarms.
            val snoozeRequestCode = (1_000_000_000 + scheduleId + System.currentTimeMillis() % 1_000_000).toInt()
            // Make sure this is unique enough for your app's needs.
            // If scheduleId + currentTime % X might repeat too often for different schedules, consider something stronger.
            // For one-off snoozes, this is usually fine.

            val snoozeMedicationAlarm = MedicationAlarm(
                scheduleId = scheduleId, // Still link it to the original schedule
                medicationName = medication.name,
                dosage = medication.dosage,
                dosageUnit = dosageUnit?.name.orEmpty(),
                scheduledTime = newSnoozeTime,
                requestCode = snoozeRequestCode
            )

            // 5. Schedule this one-time snooze alarm.
            alarmManager.scheduleAlarm(snoozeMedicationAlarm)
            Log.d("MedSchedulerService", "✅ One-time snooze alarm scheduled with RequestCode: ${snoozeMedicationAlarm.requestCode} for ${snoozeMedicationAlarm.scheduledTime}")

            // 6. IMPORTANT: Do NOT save this temporary snooze alarm to scheduledAlarmRepository.
            //    It's a one-off. It will fire and then naturally be gone.
            //    This keeps your ScheduledAlarmRecord table only for the recurring, long-term alarms.
            //    If you *did* want to track it (e.g., for showing "snoozed" state in UI),
            //    you'd need a separate mechanism or a `isTemporarySnooze` flag on ScheduledAlarmRecord.
            //    For simplicity of a pure "snooze from now", we omit saving it.

        } catch (e: Exception) {
            Log.e("MedSchedulerService", "Error creating snooze alarm for schedule ID: $scheduleId: ${e.message}", e)
        }
    }
}
