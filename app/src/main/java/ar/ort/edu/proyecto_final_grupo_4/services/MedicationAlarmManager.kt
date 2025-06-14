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
import java.time.ZoneId

class MedicationAlarmManager(
    private val context: Context,
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
) {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleAlarm(alarm: MedicationAlarm) {
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("scheduleId", alarm.scheduleId)
            putExtra("medicationName", alarm.medicationName)
            putExtra("dosage", alarm.dosage)
            putExtra("dosageUnit", alarm.dosageUnit)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = alarm.scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    Log.e("MedicationAlarmManager", "No se puede programar alarma exacta", e)
                    // Opcional: mostrar un aviso al usuario o redirigir a ajustes
                }
            } else {
                Log.w("MedicationAlarmManager", "El sistema no permite alarmas exactas.")
                // Opcional: redirigir a ajustes
                // val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                // context.startActivity(intent)
            }
        } else {
            // En versiones < Android 12 no se requiere permiso especial
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

    }

    fun cancelAlarm(scheduleId: Long) {
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun scheduleAllAlarms(alarms: List<MedicationAlarm>) {
        alarms.forEach { alarm ->
            scheduleAlarm(alarm)
        }
    }
}