package zm.farmbook.app.ui.screens

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
import zm.farmbook.app.ui.*

@Composable
fun SummaryScreen(vm: FarmViewModel, onBack: () -> Unit) {
    val rows by vm.summary.collectAsState()
    Scaffold(topBar = { FarmTopBar("Summary", onBack) }) { pad ->
        if (rows.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Nothing to summarise yet. Add a group and some records first.")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows) { r ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(r.groupName, style = MaterialTheme.typography.titleMedium)
                            Text("${r.animalType} - ${r.animalCount} animals")
                            Spacer(Modifier.height(6.dp))
                            Text("Deaths this month: ${r.deathsThisMonth}")
                            Text(
                                r.avgProduction?.let { "Average per day logged: ${formatNumber(it)} ${unitFor(r.animalType)}" }
                                    ?: "No egg/milk counts logged yet"
                            )
                        }
                    }
                }
            }
        }
    }
}
