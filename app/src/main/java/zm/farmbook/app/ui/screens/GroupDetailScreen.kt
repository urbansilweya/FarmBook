package zm.farmbook.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zm.farmbook.app.data.FarmRecord
import zm.farmbook.app.data.RecordType
import zm.farmbook.app.ui.*

@Composable
fun GroupDetailScreen(
    vm: FarmViewModel,
    groupId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddRecord: () -> Unit,
    onDeleted: () -> Unit
) {
    val group by remember(groupId) { vm.group(groupId) }.collectAsState(initial = null)
    val records by remember(groupId) { vm.records(groupId) }.collectAsState(initial = emptyList())
    var confirmDelete by remember { mutableStateOf(false) }
    val g = group

    Scaffold(
        topBar = {
            FarmTopBar(g?.name ?: "Group", onBack, actions = {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = { confirmDelete = true }) { Text("Delete") }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddRecord) { Text("+ Add record") }
        }
    ) { pad ->
        if (g == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Group not found") }
            return@Scaffold
        }
        LazyColumn(
            Modifier.padding(pad),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("${g.animalType} - ${g.animalCount} animals - started ${formatDay(g.startDate)}")
                Spacer(Modifier.height(8.dp))
                Text("History", style = MaterialTheme.typography.titleMedium)
            }
            if (records.isEmpty()) {
                item { Text("No records yet. Tap \"+ Add record\" to log a vaccination, death or count.") }
            }
            items(records, key = { it.id }) { r -> RecordCard(r, g.animalType, vm) }
        }
    }

    if (confirmDelete && g != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${g.name}?") },
            text = { Text("This also deletes all of its records and cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.deleteGroup(g, onDeleted) }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RecordCard(r: FarmRecord, animalType: String, vm: FarmViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("${r.type.label} - ${formatDay(r.date)}", style = MaterialTheme.typography.titleSmall)
            when (r.type) {
                RecordType.VACCINATION -> Text(r.note)
                RecordType.DEATH -> Text("${r.quantity.toInt()} died" + if (r.note.isNotBlank()) " (${r.note})" else "")
                RecordType.PRODUCTION -> Text("${formatNumber(r.quantity)} ${unitFor(animalType)}")
            }
            r.nextDoseDate?.let { due ->
                val label = when {
                    r.reminderDone -> "Next dose ${formatDay(due)} - done"
                    due < todayEpochDay() -> "Next dose ${formatDay(due)} - OVERDUE"
                    else -> "Next dose ${formatDay(due)}"
                }
                Text(label)
            }
            Row {
                if (r.nextDoseDate != null && !r.reminderDone) {
                    TextButton(onClick = { vm.markDone(r.id) }) { Text("Mark dose done") }
                }
                TextButton(onClick = { vm.deleteRecord(r) }) { Text("Remove") }
            }
        }
    }
}
