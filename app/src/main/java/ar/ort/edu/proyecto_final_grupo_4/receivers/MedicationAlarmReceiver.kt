package ar.ort.edu.proyecto_final_grupo_4.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import ar.ort.edu.proyecto_final_grupo_4.MainActivity
import ar.ort.edu.proyecto_final_grupo_4.R

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("med_name") ?: "Medicamento"
        val dosage = intent.getStringExtra("dosage") ?: ""

        val channelId = "medication_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intentToOpenApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fromAlarm", true)
            putExtra("scheduleId", intent.getLongExtra("scheduleId", -1))
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentToOpenApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Crear canal de notificación (solo una vez en Android 8+)
        val channel = NotificationChannel(
            channelId,
            "Recordatorios de medicación",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones para tomar tus medicamentos"
            enableVibration(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Es hora de tu medicación")
            .setContentText("$medName - $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}