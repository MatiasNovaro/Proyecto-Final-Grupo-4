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
import java.time.Duration
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

        // Verificar si el objeto alarm es mutable
        try {
            val originalTime = alarm.scheduledTime
            Log.d("AlarmDebug", "Testing mutability...")

            // Crear una nueva instancia con tiempo corregido si es necesario
            val correctedAlarm = if (!alarm.scheduledTime.isAfter(now.plusSeconds(5))) {
                Log.w("AlarmDebug", "Time needs correction")
                alarm.copy(scheduledTime = now.plusSeconds(10))
            } else {
                Log.d("AlarmDebug", "Time is valid")
                alarm
            }

            Log.d("AlarmDebug", "Final scheduledTime: ${correctedAlarm.scheduledTime}")

            // Verificar la conversión a timestamp
            val zonedDateTime = correctedAlarm.scheduledTime.atZone(ZoneId.systemDefault())
            Log.d("AlarmDebug", "ZonedDateTime: $zonedDateTime")
            Log.d("AlarmDebug", "Zone: ${ZoneId.systemDefault()}")

            val instant = zonedDateTime.toInstant()
            Log.d("AlarmDebug", "Instant: $instant")

            val triggerTime = instant.toEpochMilli()
            Log.d("AlarmDebug", "TriggerTime (millis): $triggerTime")

            // Verificar que el timestamp sea válido (futuro)
            val currentMillis = System.currentTimeMillis()
            Log.d("AlarmDebug", "Current millis: $currentMillis")
            Log.d("AlarmDebug", "Difference: ${triggerTime - currentMillis} ms")

            if (triggerTime <= currentMillis) {
                Log.e("AlarmDebug", "ERROR: Timestamp is in the past!")
                return
            }

            // Crear el intent
            val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                putExtra("scheduleId", correctedAlarm.scheduleId)
                putExtra("medicationName", correctedAlarm.medicationName)
                putExtra("dosage", correctedAlarm.dosage)
                putExtra("dosageUnit", correctedAlarm.dosageUnit)
                putExtra("originalScheduledTime", correctedAlarm.scheduledTime.toString()) // Para debugging
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                correctedAlarm.requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.d("AlarmDebug", "RequestCode: ${correctedAlarm.requestCode}")

            // Programar la alarma
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Log.d("AlarmDebug", "✅ Alarm scheduled successfully with setExactAndAllowWhileIdle")
                    } else {
                        Log.e("AlarmDebug", "❌ Cannot schedule exact alarms - permission denied")
                        return
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d("AlarmDebug", "✅ Alarm scheduled successfully (pre-Android 12)")
                }

                // Verificar que la alarma se programó
                verifyAlarmScheduled(triggerTime, correctedAlarm.medicationName)

            } catch (e: Exception) {
                Log.e("AlarmDebug", "❌ Exception scheduling alarm", e)
            }

        } catch (e: Exception) {
            Log.e("AlarmDebug", "❌ Error in alarm scheduling process", e)
        }

        Log.d("AlarmDebug", "=== FIN PROGRAMACIÓN DE ALARMA ===")
    }

    private fun verifyAlarmScheduled(triggerTime: Long, medicationName: String) {
        val futureTime = Date(triggerTime)
        Log.d("AlarmDebug", "🔍 Alarm scheduled for: $medicationName at $futureTime")

        // Log adicional para confirmar
        val minutesFromNow = (triggerTime - System.currentTimeMillis()) / 1000 / 60
        Log.d("AlarmDebug", "🕐 Alarm will trigger in $minutesFromNow minutes")
    }

    fun scheduleAllAlarms(alarms: List<MedicationAlarm>) {
        Log.d("AlarmDebug", "📋 Scheduling ${alarms.size} alarms")
        alarms.forEachIndexed { index, alarm ->
            Log.d("AlarmDebug", "🔄 Processing alarm ${index + 1}/${alarms.size}")
            scheduleAlarm(alarm)

            // Pequeño delay para evitar conflictos
            Thread.sleep(50)
        }
    }
    fun cancelAlarm(scheduleId: Long) {

        Log.w("AlarmDebug", "Mass cancellation for scheduleId $scheduleId not fully implemented due to dynamic requestCodes. Future alarms may persist if not explicitly cancelled by their exact requestCode.")

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("scheduleId", scheduleId) // Still include for potential matching
        }
        val requestCodeForPrimary = scheduleId.hashCode() // Simpler requestCode for testing mass cancel
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeForPrimary,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmDebug", "Cancelled existing PendingIntent for scheduleId $scheduleId with requestCode $requestCodeForPrimary")
        } else {
            Log.d("AlarmDebug", "No existing PendingIntent found for scheduleId $scheduleId with requestCode $requestCodeForPrimary")
        }
    }

}