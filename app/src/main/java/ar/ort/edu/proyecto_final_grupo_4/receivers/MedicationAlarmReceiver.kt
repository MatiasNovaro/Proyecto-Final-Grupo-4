package ar.ort.edu.proyecto_final_grupo_4.receivers

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import ar.ort.edu.proyecto_final_grupo_4.MainActivity
import ar.ort.edu.proyecto_final_grupo_4.R
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission


class MedicationAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "medication_channel"
        const val GROUP_KEY = "medication_group"
        private var pendingAlarms = mutableMapOf<String, MutableList<AlarmData>>()

        data class AlarmData(
            val medName: String,
            val dosage: String,
            val scheduleId: Long,
            val originalTime: String
        )
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "🔔 ===== ALARM RECEIVED =====")

        val medName = intent.getStringExtra("medicationName") ?: "Medicamento"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val scheduleId = intent.getLongExtra("scheduleId", -1)
        val originalTime = intent.getStringExtra("originalScheduledTime") ?: "Unknown"
        val triggerTimeMillis = intent.getLongExtra("triggerTimeMillis", System.currentTimeMillis()) // retrieve it

        Log.d("AlarmReceiver", "Medication: $medName, Dosage: $dosage, Schedule ID: $scheduleId")

        // Group alarms by time (round to nearest minute to handle slight timing differences)
        val timeKey = roundToNearestMinute(originalTime)
        val alarmData = AlarmData(medName, dosage, scheduleId, originalTime)

        synchronized(pendingAlarms) {
            pendingAlarms.getOrPut(timeKey) { mutableListOf() }.add(alarmData)
        }

        // Delay slightly to collect multiple alarms that might trigger at the same time
        Handler(Looper.getMainLooper()).postDelayed( {
            processPendingAlarms(context, timeKey, triggerTimeMillis)
        }, 2000) // 2 second delay to collect concurrent alarms

        Log.d("AlarmReceiver", "✅ ===== ALARM PROCESSED =====")
    }

    private fun roundToNearestMinute(timeString: String): String {
        return try {
            // Assuming format like "HH:mm" or similar
            val parts = timeString.split(":")
            "${parts[0]}:${parts[1]}" // Remove seconds if present
        } catch (e: Exception) {
            timeString
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE)
    private fun processPendingAlarms(context: Context, timeKey: String, triggerTimeMillis: Long) {
        synchronized(pendingAlarms){
            val alarms = pendingAlarms[timeKey] ?: return
            if (alarms.isEmpty()) return

            if (alarms.size == 1) {
                // Single alarm notification
                createSingleNotification(context, alarms[0], triggerTimeMillis)
            } else {
                // Multiple alarms - create grouped notification
                createGroupedNotification(context, alarms, timeKey, triggerTimeMillis)
            }

            // Play enhanced vibration (sound handled by channel now)
            playEnhancedVibration(context)

            // Clear processed alarms
            pendingAlarms.remove(timeKey)
        }
    }

    private fun createSingleNotification(
        context: Context,
        alarm: AlarmData,
        triggerTimeMillis: Any?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager) // Ensure channel exists first

        // Create intent to navigate to medication detail screen
        val clickIntent = createNavigationIntent(context, listOf(alarm.scheduleId))
        val pendingIntent = PendingIntent.getActivity(
            context,
            alarm.scheduleId.toInt(),
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create action buttons
//        val takenIntent = createActionIntent(context, "TAKEN", listOf(alarm.scheduleId))
//        val takenPendingIntent = PendingIntent.getBroadcast(
//            context,
//            (alarm.scheduleId * 10).toInt(),
//            takenIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val snoozeIntent = createActionIntent(context, "SNOOZE", listOf(alarm.scheduleId))
//        val snoozePendingIntent = PendingIntent.getBroadcast(
//            context,
//            (alarm.scheduleId * 10 + 1).toInt(),
//            snoozeIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.clock_icon) // Use a dedicated medication icon
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.clock_icon))
            .setContentTitle("💊 Hora de medicación")
            .setContentText("${alarm.medName} - ${alarm.dosage}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Es hora de tomar: ${alarm.medName}\nDosis: ${alarm.dosage}\nHora programada: ${alarm.originalTime}"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true) // Don't auto-cancel, require user action
            .setOngoing(false) // Make it persistent
            .setContentIntent(pendingIntent)
//            .addAction(NotificationCompat.Action.Builder(
//                R.drawable.clock_icon, // Use appropriate icon for "Tomado"
//                "Tomado",
//                takenPendingIntent
//            ).build())
//            .addAction(NotificationCompat.Action.Builder(
//                R.drawable.clock_icon, // Use appropriate icon for "Posponer"
//                "Posponer",
//                snoozePendingIntent
//            ).build())
            .setDefaults(0) // Set to 0 because channel handles sound/vibration
            .setGroup(GROUP_KEY)
            .build()

        notificationManager.notify((alarm.scheduleId.hashCode() + triggerTimeMillis.hashCode()), notification)
    }

    @SuppressLint("RestrictedApi")
    private fun createGroupedNotification(
        context: Context,
        alarms: List<AlarmData>,
        timeKey: String,
        triggerTimeMillis: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager) // Ensure channel exists first

        val scheduleIds = alarms.map { it.scheduleId }

        // Create intent to navigate to medication list screen with all IDs
        val clickIntent = createNavigationIntent(context, scheduleIds)
        val pendingIntent = PendingIntent.getActivity(
            context,
            timeKey.hashCode(),
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create group action intents
//        val allTakenIntent = createActionIntent(context, "ALL_TAKEN", scheduleIds)
//        val allTakenPendingIntent = PendingIntent.getBroadcast(
//            context,
//            timeKey.hashCode() * 10,
//            allTakenIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        // Create individual notifications for each medication (collapsed in group)
        alarms.forEach { alarm ->
            val individualNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.clock_icon) // Use appropriate icon
                .setContentTitle("💊 ${alarm.medName}")
                .setContentText(alarm.dosage)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup(GROUP_KEY)
                .setAutoCancel(false)
                .setDefaults(0) // Set to 0 because channel handles sound/vibration
                .build()

            notificationManager.notify((alarm.scheduleId.hashCode() + triggerTimeMillis.hashCode()), individualNotification)
        }

        // Define 'summary' BEFORE it's used in the builder
        val summary = "${alarms.size} medicamentos programados"

        val inboxStyle = NotificationCompat.InboxStyle()
            .setSummaryText("Hora programada: $timeKey")
        alarms.forEach { alarm ->
            inboxStyle.addLine("• ${alarm.medName} - ${alarm.dosage}")
        }


        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.clock_icon)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.clock_icon))
            .setContentTitle("💊 Hora de medicación")
            .setContentText(summary)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
//            .addAction(NotificationCompat.Action.Builder(
//                R.drawable.clock_icon, // Use appropriate icon
//                "Todos tomados",
//                allTakenPendingIntent
//            ).build())
            .setStyle(inboxStyle) // Apply the created InboxStyle here
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setDefaults(0) // Set to 0 because channel handles sound/vibration
            .build()

        notificationManager.notify(Int.MAX_VALUE - timeKey.hashCode(), summaryNotification)
    }

    private fun createNavigationIntent(context: Context, scheduleIds: List<Long>): Intent {
        // --- START CHANGES HERE ---
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "medication_confirmation") // Match your NavHost route
            putExtra("scheduleIds", scheduleIds.toLongArray()) // Pass as LongArray
            putExtra("fromNotification", true) // Indicate it's from a notification
        }
        return intent
        // --- END CHANGES HERE ---
    }

    private fun createActionIntent(context: Context, action: String, scheduleIds: List<Long>): Intent {
        return Intent(context, MedicationActionReceiver::class.java).apply {
            this.action = action
            putExtra("scheduleIds", scheduleIds.toLongArray())
        }
    }

    // Renamed from playEnhancedAlert to reflect it now only handles vibration
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun playEnhancedVibration(context: Context) {
        try {
            // Enhanced vibration pattern (short-long-short-long)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a custom vibration pattern for medication alerts
                val pattern = longArrayOf(0, 500, 200, 500, 200, 1000, 200, 500)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)) // Repeats from index 0
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 1000, 200, 500), 0) // Repeats from index 0
            }

            // Removed manual sound playing - channel handles it

            // CRITICAL: Add a handler to stop vibration after a duration
            // This is especially important since you now have a repeating vibration.
            Handler(Looper.getMainLooper()).postDelayed({
                vibrator.cancel()
                Log.d("AlarmReceiver", "Vibration stopped by timeout.")
            }, 15000) // Stop vibration after 15 seconds if no user interaction

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error playing enhanced vibration", e)
        }
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de medicación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones críticas para tomar medicamentos"
                enableVibration(true) // Re-enabled channel vibration, but custom pattern will override
                // enableVibration(false) // If you want *only* your custom vibration, keep this false and manually vibrate.
                // However, if you set a sound, the channel will likely vibrate with the sound.
                // Let's enable it for now to see if it helps.

                enableLights(true)
                lightColor = Color.RED
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true) // Bypass Do Not Disturb
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                ) // Channel plays the alarm sound
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

class MedicationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val scheduleIds = intent.getLongArrayExtra("scheduleIds") ?: return

        when (action) {
            "TAKEN" -> {
                // Mark single medication as taken
                markMedicationsTaken(context, scheduleIds.toList())
                dismissNotifications(context, scheduleIds.toList())
            }
            "ALL_TAKEN" -> {
                // Mark all medications as taken
                markMedicationsTaken(context, scheduleIds.toList())
                dismissNotifications(context, scheduleIds.toList())
            }
            "SNOOZE" -> {
                // Snooze for 5 minutes
                snoozeAlarms(context, scheduleIds.toList(), 5)
                dismissNotifications(context, scheduleIds.toList())
            }
        }
    }

    private fun markMedicationsTaken(context: Context, scheduleIds: List<Long>) {
        // Implement your database logic to mark medications as taken
        // Example:
        // val database = MedicationDatabase.getInstance(context)
        // scheduleIds.forEach { id ->
        //     database.medicationDao().markAsTaken(id, System.currentTimeMillis())
        // }

        Toast.makeText(context, "Medicamento(s) marcado(s) como tomado(s)", Toast.LENGTH_SHORT).show()
    }

    private fun snoozeAlarms(context: Context, scheduleIds: List<Long>, minutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val delayMillis = minutes * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + delayMillis

        scheduleIds.forEach { scheduleId ->
            val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                // Add original alarm data here
                putExtra("scheduleId", scheduleId)
                putExtra("snoozeCount", 1) // Track snooze count
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (scheduleId + 1000000).toInt(), // Different request code for snooze
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }

        Toast.makeText(context, "Recordatorio pospuesto $minutes minutos", Toast.LENGTH_SHORT).show()
    }

    private fun dismissNotifications(context: Context, scheduleIds: List<Long>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        scheduleIds.forEach { id ->
            notificationManager.cancel(id.toInt())
        }
        // Also dismiss the group summary if it exists
        // Corrected: Access GROUP_KEY from MedicationAlarmReceiver's companion object
        notificationManager.cancel(Int.MAX_VALUE - MedicationAlarmReceiver.GROUP_KEY.hashCode())
    }
}