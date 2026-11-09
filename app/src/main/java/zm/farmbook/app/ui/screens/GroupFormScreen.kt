package zm.farmbook.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zm.farmbook.app.data.FarmGroup
import zm.farmbook.app.logic.Validators
import zm.farmbook.app.ui.FarmTopBar
import zm.farmbook.app.ui.FarmViewModel
import java.time.LocalDate

/** groupId <= 0 means "add new". Fields use rememberSaveable so they survive rotation. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupFormScreen(vm: FarmViewModel, groupId: Long, onBack: () -> Unit, onSaved: () -> Unit) {
    val editing = groupId > 0
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableStateOf("") }
    var start by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var loaded by rememberSaveable { mutableStateOf(!editing) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(groupId) {
        if (!loaded) {
            vm.getGroup(groupId)?.let {
                name = it.name; type = it.animalType
                count = it.animalCount.toString()
                start = LocalDate.ofEpochDay(it.startDate).toString()
            }
            loaded = true
        }
    }

    Scaffold(topBar = { FarmTopBar(if (editing) "Edit group" else "Add group", onBack) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                name, { name = it }, label = { Text("Group name (e.g. Broilers Batch 3)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Text("Animal type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Chicken", "Goat").forEach {
                    FilterChip(selected = type == it, onClick = { type = it }, label = { Text(it) })
                }
            }
            OutlinedTextField(
                count, { count = it }, label = { Text("Number of animals") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                start, { start = it }, label = { Text("Start date (yyyy-MM-dd)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val problem = Validators.groupError(name, type, count, start)
                    error = problem
                    if (problem == null) {
                        vm.saveGroup(
                            FarmGroup(
                                id = if (editing) groupId else 0,
                                name = name.trim(),
                                animalType = type,
                                animalCount = count.trim().toInt(),
                                startDate = Validators.date(start)!!
                            ), onSaved
                        )
                    }
                }
            ) { Text("Save") }
        }
    }
}
