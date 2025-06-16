package ar.ort.edu.proyecto_final_grupo_4.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import ar.ort.edu.proyecto_final_grupo_4.MainActivity
import ar.ort.edu.proyecto_final_grupo_4.R
import android.util.Log
import java.time.LocalDateTime

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "🔔 ===== ALARM RECEIVED =====")
        Log.d("AlarmReceiver", "Intent: $intent")
        Log.d("AlarmReceiver", "Extras: ${intent.extras}")

        val medName = intent.getStringExtra("medicationName") ?: "Medicamento"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val scheduleId = intent.getLongExtra("scheduleId", -1)
        val originalTime = intent.getStringExtra("originalScheduledTime") ?: "Unknown"

        Log.d("AlarmReceiver", "Medication: $medName")
        Log.d("AlarmReceiver", "Dosage: $dosage")
        Log.d("AlarmReceiver", "Schedule ID: $scheduleId")
        Log.d("AlarmReceiver", "Original scheduled time: $originalTime")
        Log.d("AlarmReceiver", "Actual trigger time: ${LocalDateTime.now()}")

        // Crear notificación
        createNotification(context, medName, dosage, scheduleId)

        Log.d("AlarmReceiver", "📱 Notification created and displayed")
        Log.d("AlarmReceiver", "✅ ===== ALARM PROCESSED =====")
    }

    private fun createNotification(context: Context, medName: String, dosage: String, scheduleId: Long) {
        val channelId = "medication_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal si no existe
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de medicación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para tomar medicamentos"
                enableVibration(true)
                enableLights(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Hora de medicación")
            .setContentText("$medName - $dosage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val notificationId = (System.currentTimeMillis() / 1000).toInt()
        notificationManager.notify(notificationId, notification)

        Log.d("AlarmReceiver", "Notification ID: $notificationId")
    }
}