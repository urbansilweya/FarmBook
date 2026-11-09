package zm.farmbook.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import zm.farmbook.app.MainActivity
import zm.farmbook.app.data.DueItem
import zm.farmbook.app.ui.formatDay

object Notifier {
    const val CHANNEL_ID = "dose_reminders"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Dose reminders", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminds you when a vaccination or treatment dose is due" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Safe when notification permission is denied: it simply does nothing. */
    fun showDue(context: Context, items: List<DueItem>) {
        val manager = NotificationManagerCompat.from(context)
        if (items.isEmpty() || !manager.areNotificationsEnabled()) return

        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val lines = items.map { "${it.groupName}: ${it.note} due ${formatDay(it.dueDate)}" }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${items.size} dose(s) due")
            .setContentText(lines.first())
            .setStyle(NotificationCompat.InboxStyle().also { s -> lines.forEach { s.addLine(it) } })
            .setContentIntent(open)
            .setAutoCancel(true)
        try {
            manager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            // Permission revoked between the check and the call - ignore, do not crash.
        }
    }
}
