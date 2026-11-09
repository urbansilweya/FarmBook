package zm.farmbook.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zm.farmbook.app.ui.FarmTopBar
import zm.farmbook.app.ui.FarmViewModel
import zm.farmbook.app.ui.formatDay
import zm.farmbook.app.ui.todayEpochDay

@Composable
fun HomeScreen(
    vm: FarmViewModel,
    notificationsOn: Boolean,
    onRequestPermission: () -> Unit,
    onAddGroup: () -> Unit,
    onOpenGroup: (Long) -> Unit,
    onSummary: () -> Unit,
    onTestReminder: () -> Unit
) {
    val groups by vm.groups.collectAsState()

    Scaffold(
        topBar = {
            FarmTopBar("FarmBook", actions = {
                TextButton(onClick = onSummary) { Text("Summary") }
                TextButton(onClick = onTestReminder) { Text("Test") }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddGroup) { Text("+ Add group") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (!notificationsOn) {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Reminders are off. Allow notifications to be told when a dose is due.", Modifier.weight(1f))
                        TextButton(onClick = onRequestPermission) { Text("Allow") }
                    }
                }
            }

            if (groups.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No animal groups yet.\nTap \"+ Add group\" to start your first record.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groups, key = { it.group.id }) { item ->
                        Card(Modifier.fillMaxWidth().clickable { onOpenGroup(item.group.id) }) {
                            Column(Modifier.padding(16.dp)) {
                                Text(item.group.name, style = MaterialTheme.typography.titleMedium)
                                Text("${item.group.animalType} - ${item.group.animalCount} animals")
                                Spacer(Modifier.height(6.dp))
                                val due = item.nextDue
                                when {
                                    due == null -> Text("No dose scheduled")
                                    due < todayEpochDay() -> Text(
                                        "OVERDUE since ${formatDay(due)}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    else -> Text("Next dose due: ${formatDay(due)}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
