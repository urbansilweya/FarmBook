package zm.farmbook.app

import android.app.Application
import zm.farmbook.app.reminders.Notifier
import zm.farmbook.app.reminders.ReminderScheduler

class FarmBookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifier.createChannel(this)
        ReminderScheduler.schedule(this)
    }
}
