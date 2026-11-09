package zm.farmbook.app.data

import kotlinx.coroutines.flow.Flow

/** Single entry point the ViewModel uses to reach the database. */
class FarmRepository(private val dao: FarmDao) {
    val groups: Flow<List<GroupWithNext>> = dao.observeGroupsWithNext()

    fun group(id: Long) = dao.observeGroup(id)
    fun records(groupId: Long) = dao.observeRecords(groupId)
    fun deathsBetween(from: Long, to: Long) = dao.deathsBetween(from, to)
    fun averageProduction() = dao.averageProduction()

    suspend fun getGroup(id: Long) = dao.getGroup(id)
    suspend fun saveGroup(group: FarmGroup) {
        if (group.id == 0L) dao.insertGroup(group) else dao.updateGroup(group)
    }
    suspend fun deleteGroup(group: FarmGroup) = dao.deleteGroup(group)
    suspend fun addRecord(record: FarmRecord) = dao.insertRecord(record)
    suspend fun deleteRecord(record: FarmRecord) = dao.deleteRecord(record)
    suspend fun markDone(id: Long) = dao.markDone(id)
}
