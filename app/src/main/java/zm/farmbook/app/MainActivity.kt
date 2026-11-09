package zm.farmbook.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import zm.farmbook.app.reminders.ReminderScheduler
import zm.farmbook.app.ui.FarmViewModel
import zm.farmbook.app.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32))) {
                FarmBookNav()
            }
        }
    }
}

@Composable
fun FarmBookNav() {
    val context = LocalContext.current
    val nav = rememberNavController()
    val vm: FarmViewModel = viewModel()

    // Notification permission: ask once on Android 13+, but the app works fine if denied.
    var notificationsOn by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    LaunchedEffect(Unit) {
        if (!notificationsOn && Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val idArg = listOf(navArgument("id") { type = NavType.LongType })

    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                vm = vm,
                notificationsOn = notificationsOn,
                onRequestPermission = {
                    if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else Toast.makeText(context, "Enable notifications in system settings", Toast.LENGTH_LONG).show()
                },
                onAddGroup = { nav.navigate("group_form/0") },
                onOpenGroup = { nav.navigate("detail/$it") },
                onSummary = { nav.navigate("summary") },
                onTestReminder = {
                    ReminderScheduler.runNow(context)
                    Toast.makeText(context, "Checked for due doses (due today/tomorrow or overdue)", Toast.LENGTH_LONG).show()
                }
            )
        }
        composable("group_form/{id}", idArg) { e ->
            GroupFormScreen(vm, e.arguments!!.getLong("id"), onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
        }
        composable("detail/{id}", idArg) { e ->
            val id = e.arguments!!.getLong("id")
            GroupDetailScreen(
                vm, id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("group_form/$id") },
                onAddRecord = { nav.navigate("record_form/$id") },
                onDeleted = { nav.popBackStack("home", false) }
            )
        }
        composable("record_form/{id}", idArg) { e ->
            RecordFormScreen(vm, e.arguments!!.getLong("id"), onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
        }
        composable("summary") { SummaryScreen(vm, onBack = { nav.popBackStack() }) }
    }
}
