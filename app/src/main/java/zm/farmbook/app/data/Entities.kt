package zm.farmbook.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One flock / herd, e.g. "Broilers Batch 3". Dates are stored as epoch days. */
@Entity(tableName = "farm_groups")
data class FarmGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val animalType: String,      // "Chicken" or "Goat"
    val animalCount: Int,
    val startDate: Long
)

enum class RecordType(val label: String) {
    VACCINATION("Vaccination"),
    DEATH("Death"),
    PRODUCTION("Egg / milk count")
}

/** A health or production event that belongs to exactly one group. */
@Entity(
    tableName = "records",
    foreignKeys = [ForeignKey(
        entity = FarmGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("groupId"), Index("date"), Index("type")]
)
data class FarmRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val type: RecordType,
    val quantity: Double = 0.0,   // deaths, eggs or litres
    val note: String = "",        // vaccine name or comment
    val date: Long,
    val nextDoseDate: Long? = null,
    val reminderDone: Boolean = false
)
