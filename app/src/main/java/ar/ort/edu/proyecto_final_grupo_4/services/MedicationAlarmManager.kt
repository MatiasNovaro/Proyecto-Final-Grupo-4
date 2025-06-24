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

class MedicationAlarmManager(
    private val context: Context,
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
) {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleAlarm(alarm: MedicationAlarm) {
        Log.d("AlarmDebug", "=== INICIANDO PROGRAMACIÓN DE ALARMA ===")
        Log.d("AlarmDebug", "Alarm object: $alarm")
        Log.d("AlarmDebug", "Original scheduledTime: ${alarm.scheduledTime}")
        Log.d("AlarmDebug", "scheduledTime class: ${alarm.scheduledTime::class.java}")

        val now = LocalDateTime.now()
        Log.d("AlarmDebug", "Current time: $now")

        // No need for 'correctedAlarm' logic here; AlarmCalculatorService handles future times.
        // If alarm.scheduledTime is still in the past, it means AlarmCalculatorService failed.
        if (!alarm.scheduledTime.isAfter(now.plusSeconds(5))) {
            Log.e("AlarmDebug", "ERROR: Attempted to schedule alarm in the past or too soon: ${alarm.scheduledTime}")
            return // Prevent scheduling alarms that are already past
        }

        // --- Start of relevant changes for SecurityException ---

        // Check for permission on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 and above
            if (!alarmManager.canScheduleExactAlarms()) {
                // Permission not granted. Log and potentially inform the user.
                Log.e("AlarmDebug", "❌ SCHEDULE_EXACT_ALARM permission not granted. Cannot schedule exact alarm.")
                // You might want to show a notification or a dialog prompting the user to grant this permission.
                // Example: context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        // --- End of relevant changes ---


        // Verify the conversion to timestamp
        val zonedDateTime = alarm.scheduledTime.atZone(ZoneId.systemDefault())
        Log.d("AlarmDebug", "ZonedDateTime: $zonedDateTime")
        Log.d("AlarmDebug", "Zone: ${ZoneId.systemDefault()}")

        val instant = zonedDateTime.toInstant()
        Log.d("AlarmDebug", "Instant: $instant")

        val triggerTime = instant.toEpochMilli()
        Log.d("AlarmDebug", "TriggerTime (millis): $triggerTime")

        // Verify that the timestamp is valid (future) - Redundant with initial check but harmless
        val currentMillis = System.currentTimeMillis()
        Log.d("AlarmDebug", "Current millis: $currentMillis")
        Log.d("AlarmDebug", "Difference: ${triggerTime - currentMillis} ms")

        if (triggerTime <= currentMillis) { // This check should ideally be handled by AlarmCalculatorService
            Log.e("AlarmDebug", "ERROR: Timestamp is in the past AFTER conversion! This should not happen if AlarmCalculatorService works correctly.")
            return
        }

        // Create the intent
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("scheduleId", alarm.scheduleId)
            putExtra("medicationName", alarm.medicationName)
            putExtra("dosage", alarm.dosage)
            putExtra("dosageUnit", alarm.dosageUnit)
            putExtra("originalScheduledTime", alarm.scheduledTime.toString())
            putExtra("triggerTimeMillis", triggerTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmDebug", "RequestCode: ${alarm.requestCode}")

        // Schedule the alarm
        try {
            // No longer need the redundant Build.VERSION.SDK_INT check here if handled above
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("AlarmDebug", "✅ Alarm scheduled successfully for ${alarm.medicationName} at ${alarm.scheduledTime}")

            // Verify that the alarm was scheduled
            verifyAlarmScheduled(triggerTime, alarm.medicationName)

        } catch (e: SecurityException) {
            // Explicitly catch SecurityException for exact alarms
            Log.e("AlarmDebug", "❌ SecurityException when scheduling exact alarm. User likely denied SCHEDULE_EXACT_ALARM.", e)
            // You might want to log this to analytics or show a user-friendly error.
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

    fun scheduleAllAlarms(alarms: List<MedicationAlarm>) {
        Log.d("AlarmDebug", "📋 Scheduling ${alarms.size} alarms")
        alarms.forEachIndexed { index, alarm ->
            Log.d("AlarmDebug", "🔄 Processing alarm ${index + 1}/${alarms.size}")
            scheduleAlarm(alarm)
        }
    }

    fun cancelAlarm(requestCode: Int) {
        Log.d("AlarmDebug", "Attempting to cancel alarm with requestCode $requestCode")
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmDebug", "✅ Cancelled existing PendingIntent with requestCode $requestCode")
        } else {
            Log.d("AlarmDebug", "❌ No existing PendingIntent found for requestCode $requestCode. It might have already fired or was never scheduled.")
        }
    }
}