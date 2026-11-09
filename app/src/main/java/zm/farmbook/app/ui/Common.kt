package zm.farmbook.app.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

fun formatDay(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dayFormat)
fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
fun unitFor(animalType: String) = if (animalType == "Goat") "litres" else "eggs"
fun formatNumber(d: Double): String = if (d % 1.0 == 0.0) d.toInt().toString() else "%.1f".format(d)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmTopBar(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = actions
    )
}
