package zm.farmbook.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import zm.farmbook.app.data.FarmRecord
import zm.farmbook.app.data.RecordType
import zm.farmbook.app.logic.Validators
import zm.farmbook.app.ui.FarmTopBar
import zm.farmbook.app.ui.FarmViewModel
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordFormScreen(vm: FarmViewModel, groupId: Long, onBack: () -> Unit, onSaved: () -> Unit) {
    var typeName by rememberSaveable { mutableStateOf(RecordType.VACCINATION.name) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var quantity by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var nextDose by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val type = RecordType.valueOf(typeName)

    Scaffold(topBar = { FarmTopBar("Add record", onBack) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("What happened?")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordType.values().forEach {
                    FilterChip(selected = type == it, onClick = { typeName = it.name; error = null }, label = { Text(it.label) })
                }
            }
            OutlinedTextField(
                date, { date = it }, label = { Text("Date (yyyy-MM-dd)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            when (type) {
                RecordType.VACCINATION -> {
                    OutlinedTextField(
                        note, { note = it }, label = { Text("Vaccine / treatment name") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        nextDose, { nextDose = it },
                        label = { Text("Next dose date (optional, yyyy-MM-dd)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
                RecordType.DEATH -> {
                    OutlinedTextField(
                        quantity, { quantity = it }, label = { Text("Number of deaths") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        note, { note = it }, label = { Text("Cause / comment (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                RecordType.PRODUCTION -> OutlinedTextField(
                    quantity, { quantity = it }, label = { Text("Eggs collected or litres of milk") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val problem = Validators.recordError(type, date, quantity, note, nextDose)
                    error = problem
                    if (problem == null) {
                        vm.addRecord(
                            FarmRecord(
                                groupId = groupId,
                                type = type,
                                quantity = if (type == RecordType.VACCINATION) 0.0 else quantity.trim().toDouble(),
                                note = note.trim(),
                                date = Validators.date(date)!!,
                                nextDoseDate = if (type == RecordType.VACCINATION && nextDose.isNotBlank())
                                    Validators.date(nextDose) else null
                            ), onSaved
                        )
                    }
                }
            ) { Text("Save record") }
        }
    }
}
