package ar.ort.edu.proyecto_final_grupo_4.services

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import ar.ort.edu.proyecto_final_grupo_4.domain.model.MedicationAlarm
import ar.ort.edu.proyecto_final_grupo_4.receivers.MedicationAlarmReceiver
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import ar.ort.edu.proyecto_final_grupo_4.domain.model.ScheduledAlarmRecord
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.MedicationRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduleRepository
import ar.ort.edu.proyecto_final_grupo_4.domain.repository.ScheduledAlarmRepository
import java.time.format.DateTimeFormatter

class MedicationAlarmManager(
    private val context: Context,
    private val scheduledAlarmRepository: ScheduledAlarmRepository
) {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Define a formatter for HH:mm format
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm") // You can change this pattern if needed (e.g., "hh:mm a" for AM/PM)

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    suspend fun scheduleAlarm(alarm: MedicationAlarm) {
        Log.d("AlarmDebug", "=== INICIANDO PROGRAMACIÓN DE ALARMA ===")
        Log.d("AlarmDebug", "Alarm object: $alarm")
        Log.d("AlarmDebug", "Original scheduledTime: ${alarm.scheduledTime}")
        Log.d("AlarmDebug", "scheduledTime class: ${alarm.scheduledTime::class.java}")

        val now = LocalDateTime.now()
        Log.d("AlarmDebug", "Current time: $now")

        if (!alarm.scheduledTime.isAfter(now.plusSeconds(5))) {
            Log.e("AlarmDebug", "ERROR: Attempted to schedule alarm in the past or too soon: ${alarm.scheduledTime}")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("AlarmDebug", "❌ SCHEDULE_EXACT_ALARM permission not granted. Cannot schedule exact alarm.")
                return
            }
        }

        val zonedDateTime = alarm.scheduledTime.atZone(ZoneId.systemDefault())
        val instant = zonedDateTime.toInstant()
        val triggerTime = instant.toEpochMilli()

        val currentMillis = System.currentTimeMillis()
        if (triggerTime <= currentMillis) {
            Log.e("AlarmDebug", "ERROR: Timestamp is in the past AFTER conversion! This should not happen if AlarmCalculatorService works correctly.")
            return
        }

        // Format the scheduled time for display in the notification
        val formattedScheduledTime = alarm.scheduledTime.format(timeFormatter)

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = "ar.ort.edu.proyecto_final_grupo_4.MEDICATION_ALARM"
            putExtra("scheduleId", alarm.scheduleId)
            putExtra("medicationName", alarm.medicationName)
            putExtra("dosage", alarm.dosage)
            putExtra("dosageUnit", alarm.dosageUnit)
            putExtra("originalScheduledTime", formattedScheduledTime) // <--- Use the formatted time here
            putExtra("triggerTimeMillis", triggerTime)
            putExtra("requestCode", alarm.requestCode)
            putExtra("isSnoozeAlarm", alarm.isSnoozeAlarm)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmDebug", "RequestCode: ${alarm.requestCode}")

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("AlarmDebug", "✅ Alarm scheduled successfully for ${alarm.medicationName} at ${alarm.scheduledTime} (Formatted: $formattedScheduledTime)")

            // --- SAVE THE SCHEDULED ALARM RECORD HERE ---
            val record = ScheduledAlarmRecord(
                scheduleId = alarm.scheduleId,
                medicationId = alarm.medicationId,
                requestCode = alarm.requestCode,
                scheduledTime = alarm.scheduledTime, // Keep original LocalDateTime for internal logic/re-scheduling
                medicationName = alarm.medicationName,
                dosage = alarm.dosage,
                dosageUnit = alarm.dosageUnit,
                isSnoozeAlarm = alarm.isSnoozeAlarm
            )
            scheduledAlarmRepository.insertScheduledAlarmRecord(record)
            Log.d("AlarmDebug", "ScheduledAlarmRecord saved for ReqCode: ${record.requestCode}")

            verifyAlarmScheduled(triggerTime, alarm.medicationName)

        } catch (e: SecurityException) {
            Log.e("AlarmDebug", "❌ SecurityException when scheduling exact alarm. User likely denied SCHEDULE_EXACT_ALARM.", e)
        } catch (e: Exception) {
            Log.e("AlarmDebug", "❌ General Exception scheduling alarm", e)
        }

        Log.d("AlarmDebug", "=== FIN PROGRAMACIÓN DE ALARMA ===")
    }

    private fun verifyAlarmScheduled(triggerTime: Long, medicationName: String) {
        val futureTime = Date(triggerTime)
        Log.d("AlarmDebug", "🔍 Alarm scheduled for: $medicationName at $futureTime")

        val minutesFromNow = (triggerTime - System.currentTimeMillis()) / 1000 / 60
        Log.d("AlarmDebug", "🕐 Alarm will trigger in $minutesFromNow minutes")
    }

    suspend fun scheduleAllAlarms(alarms: List<MedicationAlarm>) {
        Log.d("AlarmDebug", "📋 Scheduling ${alarms.size} alarms")
        alarms.forEachIndexed { index, alarm ->
            Log.d("AlarmDebug", "🔄 Processing alarm ${index + 1}/${alarms.size}")
            scheduleAlarm(alarm)
        }
    }

    suspend fun cancelAlarm(record: ScheduledAlarmRecord) { // Accept the full record
        Log.d("AlarmDebug", "Attempting to cancel alarm with record: ${record.requestCode} (Med: ${record.medicationName})")

        val triggerTime = record.scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = "ar.ort.edu.proyecto_final_grupo_4.MEDICATION_ALARM" // MUST match
            putExtra("scheduleId", record.scheduleId)
            putExtra("medicationName", record.medicationName)
            putExtra("dosage", record.dosage)
            putExtra("dosageUnit", record.dosageUnit)
            // Ensure originalScheduledTime matches the format used in scheduleAlarm!
            putExtra("originalScheduledTime", record.scheduledTime.format(timeFormatter)) // <--- Use the formatter here too for cancellation consistency
            putExtra("requestCode", record.requestCode)
            putExtra("isSnoozeAlarm", record.isSnoozeAlarm)
            putExtra("triggerTimeMillis", triggerTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            record.requestCode, // Use the record's specific request code
            intent,             // The intent MUST be identical (same extras)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Ensure flags match scheduling
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmDebug", "✅ Cancelled existing PendingIntent for record: ${record.requestCode}")
        } else {
            Log.d("AlarmDebug", "❌ No existing PendingIntent found for record: ${record.requestCode}. It might have already fired or was never scheduled.")
        }

        try {
            scheduledAlarmRepository.deleteScheduledAlarmRecordByRequestCode(record.requestCode)
            Log.d("AlarmDebug", "✅ Removed record from database for ReqCode: ${record.requestCode}")
        } catch (e: Exception) {
            Log.e("AlarmDebug", "❌ Error deleting scheduled alarm record from database for ReqCode: ${record.requestCode}", e)
        }
    }
}