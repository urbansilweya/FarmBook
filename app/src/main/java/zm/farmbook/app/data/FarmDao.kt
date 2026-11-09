package zm.farmbook.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** Group plus its earliest pending dose date (null when none). */
data class GroupWithNext(
    @Embedded val group: FarmGroup,
    val nextDue: Long?
)

data class GroupTotal(val groupId: Long, val total: Double)

data class DueItem(val groupName: String, val dueDate: Long, val note: String)

@Dao
interface FarmDao {

    // ---- groups ----
    @Insert suspend fun insertGroup(group: FarmGroup): Long
    @Update suspend fun updateGroup(group: FarmGroup)
    @Delete suspend fun deleteGroup(group: FarmGroup)

    @Query("SELECT * FROM farm_groups WHERE id = :id")
    suspend fun getGroup(id: Long): FarmGroup?

    @Query("SELECT * FROM farm_groups WHERE id = :id")
    fun observeGroup(id: Long): Flow<FarmGroup?>

    @Query(
        """SELECT g.*, 
           (SELECT MIN(r.nextDoseDate) FROM records r
             WHERE r.groupId = g.id AND r.nextDoseDate IS NOT NULL AND r.reminderDone = 0) AS nextDue
           FROM farm_groups g ORDER BY g.name COLLATE NOCASE"""
    )
    fun observeGroupsWithNext(): Flow<List<GroupWithNext>>

    // ---- records ----
    @Insert suspend fun insertRecord(record: FarmRecord)
    @Delete suspend fun deleteRecord(record: FarmRecord)

    @Query("UPDATE records SET reminderDone = 1 WHERE id = :id")
    suspend fun markDone(id: Long)

    @Query("SELECT * FROM records WHERE groupId = :groupId ORDER BY date DESC, id DESC")
    fun observeRecords(groupId: Long): Flow<List<FarmRecord>>

    // ---- summary queries (by type and date) ----
    @Query(
        """SELECT groupId, SUM(quantity) AS total FROM records
           WHERE type = 'DEATH' AND date BETWEEN :from AND :to GROUP BY groupId"""
    )
    fun deathsBetween(from: Long, to: Long): Flow<List<GroupTotal>>

    @Query("SELECT groupId, AVG(quantity) AS total FROM records WHERE type = 'PRODUCTION' GROUP BY groupId")
    fun averageProduction(): Flow<List<GroupTotal>>

    // ---- used by the background worker ----
    @Query(
        """SELECT g.name AS groupName, r.nextDoseDate AS dueDate, r.note AS note
           FROM records r JOIN farm_groups g ON g.id = r.groupId
           WHERE r.reminderDone = 0 AND r.nextDoseDate IS NOT NULL AND r.nextDoseDate <= :untilDay
           ORDER BY r.nextDoseDate"""
    )
    suspend fun dueItems(untilDay: Long): List<DueItem>
}
