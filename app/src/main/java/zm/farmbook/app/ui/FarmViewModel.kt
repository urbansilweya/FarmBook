package zm.farmbook.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import zm.farmbook.app.data.AppDatabase
import zm.farmbook.app.data.FarmGroup
import zm.farmbook.app.data.FarmRecord
import zm.farmbook.app.data.FarmRepository
import java.time.LocalDate

data class SummaryRow(
    val groupName: String,
    val animalType: String,
    val animalCount: Int,
    val deathsThisMonth: Int,
    val avgProduction: Double?
)

class FarmViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FarmRepository(AppDatabase.get(app).dao())

    val groups = repo.groups.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<List<SummaryRow>> = run {
        val first = LocalDate.now().withDayOfMonth(1)
        val last = first.plusMonths(1).minusDays(1)
        combine(
            repo.groups,
            repo.deathsBetween(first.toEpochDay(), last.toEpochDay()),
            repo.averageProduction()
        ) { groups, deaths, averages ->
            val deathMap = deaths.associate { it.groupId to it.total.toInt() }
            val avgMap = averages.associate { it.groupId to it.total }
            groups.map {
                SummaryRow(
                    it.group.name, it.group.animalType, it.group.animalCount,
                    deathMap[it.group.id] ?: 0, avgMap[it.group.id]
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun group(id: Long) = repo.group(id)
    fun records(groupId: Long) = repo.records(groupId)
    suspend fun getGroup(id: Long) = repo.getGroup(id)

    fun saveGroup(group: FarmGroup, onDone: () -> Unit) = viewModelScope.launch {
        repo.saveGroup(group); onDone()
    }
    fun deleteGroup(group: FarmGroup, onDone: () -> Unit) = viewModelScope.launch {
        repo.deleteGroup(group); onDone()
    }
    fun addRecord(record: FarmRecord, onDone: () -> Unit) = viewModelScope.launch {
        repo.addRecord(record); onDone()
    }
    fun deleteRecord(record: FarmRecord) = viewModelScope.launch { repo.deleteRecord(record) }
    fun markDone(id: Long) = viewModelScope.launch { repo.markDone(id) }
}
