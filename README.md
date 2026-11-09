# FarmBook

**BSE 3340 Mobile Application Development - Zambia University College of Technology**
Group project - due Friday, 30 October 2026

FarmBook helps small-scale poultry and goat farmers in Zambia keep digital records of vaccinations, deaths and egg/milk counts, and reminds them **before the next dose is due, even when the app is closed**.

## Team

| No. | Full name | Student number |
| --- | --- | --- |
| 1 |  |  |
| 2 |  |  |
| 3 |  |  |
| 4 |  |  |
| 5 |  |  |

## Features (matches the approved proposal)

| Screen | What it does |
| --- | --- |
| **Home** | Lists animal groups (e.g. Broilers Batch 3, Goats) with the next due dose, or "OVERDUE" in red. Empty state message when there are no groups. |
| **Add / Edit Group** | Name, animal type, number of animals, start date. Rejects empty fields and non-positive numbers with a clear message. |
| **Group Detail** | Scrollable history of vaccinations, deaths and egg/milk counts. Edit or delete the group, remove a record, mark a dose as done. |
| **Add Record** | Log a vaccination (with optional next-dose date), a death, or an egg/milk count. Validated. |
| **Summary** | Per group: deaths this month and average eggs/litres per day logged. |

Also handled: data survives rotation and app restarts (Room + `rememberSaveable`), empty states, and **denied notification permission** (a banner appears, nothing crashes).

## Tech stack

- Kotlin, Android Studio, Jetpack Compose (Material 3), Navigation Compose
- **Room** for local storage (research topic 2)
- **WorkManager** + notifications for background reminders (research topic 1)
- MVVM: UI (`ui/`) -> `FarmViewModel` -> `FarmRepository` -> Room DAO (`data/`)

## Project structure

```
app/src/main/java/zm/farmbook/app/
  FarmBookApp.kt            Application: creates notification channel, schedules WorkManager
  MainActivity.kt           Navigation graph + notification permission request
  data/                     Entities, FarmDao (SQL), AppDatabase, FarmRepository
  logic/Validators.kt       Pure validation functions (unit tested)
  reminders/                ReminderWorker, ReminderScheduler, Notifier
  ui/                       FarmViewModel, common helpers
  ui/screens/               Home, GroupForm, GroupDetail, RecordForm, Summary
app/src/test/.../ValidatorsTest.kt
```

## How to run

**Requirements:** Android Studio Koala (2024.1) or newer, JDK 17 (bundled with Android Studio), internet for the first Gradle sync, and either an emulator (API 26+) or a phone with USB debugging.

1. Unzip the project. In Android Studio choose **File > Open** and select the `FarmBook` folder (the one containing `settings.gradle.kts`).
2. Wait for **Gradle sync** to finish (first time takes a few minutes while it downloads dependencies). If asked to upgrade the Android Gradle Plugin, you can choose **Don't remind me** - the project works as-is.
3. Pick a device (Device Manager > Create device, e.g. Pixel 6, API 34) and press **Run**.
4. When asked, **Allow** notifications (Android 13+).

## Quick test script (for your demo)

1. Add a group: *Broilers Batch 3 / Chicken / 200 / today*. Try saving with the name empty and with count `0` - you should see error messages.
2. Open the group > **+ Add record** > Vaccination, vaccine `Newcastle`, next dose date = **today or tomorrow**.
3. Go back to Home: the group shows the next dose date. Use a past date to see **OVERDUE**.
4. Tap **Test** on Home: the app runs the WorkManager job immediately and a notification "1 dose(s) due" appears. Close the app completely and tap Test first, or wait for the 12-hourly job, to show it works while closed.
5. Add a Death record and a Production record (e.g. `45` eggs) then open **Summary**.
6. Rotate the device and force-close/reopen the app: data is still there.
7. Deny notifications (Settings > Apps > FarmBook > Notifications off): the red banner appears and nothing crashes.

Run the unit tests with **right-click `app/src/test` > Run Tests**, or `./gradlew test` in a terminal.

## Notes and limitations

- WorkManager checks about every 12 hours (the minimum periodic interval is 15 minutes) and Android may delay work to save battery, so reminders are reliable but not to-the-minute. See `RESEARCH_NOTE.md`.
- Dates are typed as `yyyy-MM-dd` to keep the app simple and testable. A date picker is a good future improvement.
- Average production is per day that a count was logged, not per calendar day.
- "Next dose" shows the earliest dose not yet marked done.

## GitHub

```
git init
git add .
git commit -m "FarmBook initial version"
git branch -M main
git remote add origin https://github.com/<your-team>/farmbook.git
git push -u origin main
```
Commit regularly and have every member commit their own work (the course expects visible contributions from all five).
