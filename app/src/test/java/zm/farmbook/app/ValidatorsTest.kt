package zm.farmbook.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import zm.farmbook.app.data.RecordType
import zm.farmbook.app.logic.Validators

class ValidatorsTest {
    @Test fun emptyGroupNameRejected() =
        assertNotNull(Validators.groupError("", "Chicken", "10", "2026-10-01"))

    @Test fun zeroAndNegativeCountsRejected() {
        assertNotNull(Validators.groupError("A", "Goat", "0", "2026-10-01"))
        assertNotNull(Validators.groupError("A", "Goat", "-5", "2026-10-01"))
        assertNotNull(Validators.groupError("A", "Goat", "abc", "2026-10-01"))
    }

    @Test fun validGroupAccepted() =
        assertNull(Validators.groupError("Broilers Batch 3", "Chicken", "200", "2026-10-01"))

    @Test fun badDateRejected() = assertNull(Validators.date("30/10/2026"))

    @Test fun deathNeedsPositiveWholeNumber() {
        assertNotNull(Validators.recordError(RecordType.DEATH, "2026-10-01", "1.5", "", ""))
        assertNull(Validators.recordError(RecordType.DEATH, "2026-10-01", "2", "", ""))
    }

    @Test fun nextDoseCannotBeBeforeVaccination() {
        assertNotNull(Validators.recordError(RecordType.VACCINATION, "2026-10-10", "", "Newcastle", "2026-10-01"))
        assertNull(Validators.recordError(RecordType.VACCINATION, "2026-10-10", "", "Newcastle", "2026-10-24"))
    }

    @Test fun positiveDoubleParsing() = assertEquals(2.5, Validators.positiveDouble("2.5")!!, 0.0)
}
