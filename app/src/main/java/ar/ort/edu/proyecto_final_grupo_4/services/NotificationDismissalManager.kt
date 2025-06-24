package ar.ort.edu.proyecto_final_grupo_4.services

import android.app.NotificationManager
import android.content.Context
import ar.ort.edu.proyecto_final_grupo_4.receivers.MedicationAlarmReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDismissalManager @Inject constructor(
    private val context: Context
) {
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun dismissMedicationNotification(scheduleId: Long) {
        notificationManager.cancel(scheduleId.toInt())
        // Also try to cancel the group summary if it was the last notification in the group
        notificationManager.cancel(Int.MAX_VALUE - MedicationAlarmReceiver.GROUP_KEY.hashCode())
    }

    fun dismissGroupSummaryNotification(timeKey: String) {
        notificationManager.cancel(Int.MAX_VALUE - timeKey.hashCode())
    }

    fun dismissAllMedicationNotificationsForGroup(scheduleIds: List<Long>) {
        scheduleIds.forEach { id ->
            notificationManager.cancel(id.toInt())
        }
        // Always dismiss the group summary when dismissing all
        notificationManager.cancel(Int.MAX_VALUE - MedicationAlarmReceiver.GROUP_KEY.hashCode())
    }
}