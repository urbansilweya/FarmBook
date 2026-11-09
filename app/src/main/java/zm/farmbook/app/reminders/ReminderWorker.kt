package zm.farmbook.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import zm.farmbook.app.data.AppDatabase
import java.time.LocalDate

/** Runs in the background (even when the app is closed) and posts a notification for doses due today/tomorrow or overdue. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val tomorrow = LocalDate.now().toEpochDay() + 1
            val due = AppDatabase.get(applicationContext).dao().dueItems(tomorrow)
            Notifier.createChannel(applicationContext)
            Notifier.showDue(applicationContext, due)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
