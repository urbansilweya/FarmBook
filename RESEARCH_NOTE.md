# Research Note: WorkManager and Room

BSE 3340 - FarmBook group project

This note covers the two topics that were not taught in class. Each section says what it is, why FarmBook needs it, how we used it (with file names), and its limits.

---

## 1. WorkManager (background tasks and notifications)

### Why we needed it
A farmer must be reminded of a due dose **while the app is closed**. Normal app code only runs while the app is open, so we need Android to run a job for us later.

### What it is
WorkManager is the Android Jetpack library for **deferrable, guaranteed background work**. Work you give it keeps running after the app is closed and even after the phone restarts. It chooses the right system mechanism for the Android version, so we do not manage that ourselves.

### How FarmBook uses it
- `ReminderWorker` (`reminders/ReminderWorker.kt`) extends `CoroutineWorker`. In `doWork()` it asks Room for doses that are due tomorrow or earlier and not yet marked done, then asks `Notifier` to show a notification.
- `ReminderScheduler.schedule()` creates a `PeriodicWorkRequest` that repeats every 12 hours and enqueues it with `enqueueUniquePeriodicWork(..., ExistingPeriodicWorkPolicy.KEEP, ...)`. `KEEP` stops duplicate jobs being created each time the app opens.
- It is called from `FarmBookApp.onCreate()` so scheduling happens once at app start.
- `ReminderScheduler.runNow()` enqueues a `OneTimeWorkRequest` so we can demo the reminder immediately (the **Test** button).
- Notifications need a **channel** (Android 8+) and the **POST_NOTIFICATIONS runtime permission** (Android 13+). `Notifier` creates the channel and checks `areNotificationsEnabled()` before posting, so a denied permission never crashes the app. The UI shows a banner offering to ask again.

### Things we learned / limits
- The minimum repeat interval for periodic work is 15 minutes. We chose 12 hours because dose dates are in days.
- WorkManager is **not exact**. Android (Doze, battery saver) can delay jobs, so a reminder may arrive later than the planned time. For exact-time alarms `AlarmManager` is the alternative, but it needs extra permissions and drains more battery. For farm dose dates "within the day" is good enough.
- A worker must be fast and must return `Result.success()`, `retry()` or `failure()`. We return `retry()` if the database read throws.
- Workers cannot touch the UI; they talk to the database and the notification system only.

### Sources
- Android Developers: *Schedule tasks with WorkManager* - https://developer.android.com/topic/libraries/architecture/workmanager
- Android Developers: *Define work requests / Recurring work* - https://developer.android.com/topic/libraries/architecture/workmanager/how-to/define-work
- Android Developers: *Notification runtime permission* - https://developer.android.com/develop/ui/views/notifications/notification-permission
- Official sample: Sunflower app (uses WorkManager and Room) - https://github.com/android/sunflower

---

## 2. Room database

### Why we needed it
FarmBook stores **related data**: one group has many records. We must ask questions such as "deaths in this group this month" or "next dose for each group". Simple storage such as SharedPreferences stores key-value pairs and cannot answer these questions without loading and looping through everything.

### What it is
Room is the Jetpack library that sits on top of SQLite. We describe tables with Kotlin classes, write SQL in annotated functions, and Room checks the SQL **at compile time** and generates the boilerplate code.

### How FarmBook uses it
- **Entities** (`data/Entities.kt`): `FarmGroup` (table `farm_groups`) and `FarmRecord` (table `records`). `FarmRecord.groupId` is a **foreign key** to `FarmGroup` with `onDelete = CASCADE`, so deleting a group removes its records. Indexes on `groupId`, `date` and `type` speed up the queries below.
- **DAO** (`data/FarmDao.kt`): 
  - `observeGroupsWithNext()` uses a subquery to get each group's earliest pending `nextDoseDate` for the Home screen.
  - `deathsBetween(from, to)` filters by **type and date** and uses `SUM ... GROUP BY groupId` for the Summary screen.
  - `averageProduction()` uses `AVG` for eggs/litres.
  - `dueItems(untilDay)` joins records with groups for the WorkManager job.
- **Flow**: DAO functions that return `Flow` re-emit automatically when data changes, so screens update without manual refreshing. `suspend` functions run off the main thread.
- **Database** (`data/AppDatabase.kt`): a singleton so the app and the background worker share one database file (`farmbook.db`), which persists across rotation and restarts.
- **Dates** are stored as epoch-day numbers (`Long`), which makes "between" comparisons simple.
- **Architecture**: screens never touch the DAO. They use `FarmViewModel`, which uses `FarmRepository`, which uses the DAO.

### Things we learned / limits
- Changing a table later requires a **migration** and a higher database `version`. We set `exportSchema = false` and version 1 for this project; a real release should export schemas and write migrations.
- `groups` is a reserved word in newer SQLite versions, so the table is named `farm_groups`.
- Enums are stored by name (`"DEATH"`), which is why the SQL compares with `'DEATH'`; renaming an enum value would break old data.

### Sources
- Android Developers: *Save data in a local database using Room* - https://developer.android.com/training/data-storage/room
- Android Developers: *Define relationships between objects* - https://developer.android.com/training/data-storage/room/relationships
- Official samples: Android Architecture Components samples - https://github.com/android/architecture-components-samples

---

## Team reflection (edit before submitting)

Add 3-5 sentences in your own words: what was hardest about each topic, what bug you hit, and what you would improve (for example a date picker, export to CSV, or SMS reminders for farmers without data).

*Please open each link and confirm it still works before you submit; documentation pages move over time.*
