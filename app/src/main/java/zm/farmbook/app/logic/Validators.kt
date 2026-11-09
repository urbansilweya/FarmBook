package zm.farmbook.app.logic

import zm.farmbook.app.data.RecordType
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Pure functions (no Android classes) so they can be unit tested on the JVM. */
object Validators {

    fun positiveInt(s: String): Int? = s.trim().toIntOrNull()?.takeIf { it > 0 }
    fun positiveDouble(s: String): Double? = s.trim().toDoubleOrNull()?.takeIf { it > 0.0 }

    /** Accepts yyyy-MM-dd and returns epoch day, or null if invalid. */
    fun date(s: String): Long? = try {
        LocalDate.parse(s.trim()).toEpochDay()
    } catch (e: DateTimeParseException) {
        null
    }

    fun groupError(name: String, type: String, count: String, start: String): String? = when {
        name.isBlank() -> "Please enter a group name."
        type.isBlank() -> "Please choose an animal type."
        count.isBlank() -> "Please enter the number of animals."
        positiveInt(count) == null -> "Number of animals must be a whole number greater than 0."
        start.isBlank() -> "Please enter a start date."
        date(start) == null -> "Start date must be a valid date like 2026-10-30."
        else -> null
    }

    fun recordError(type: RecordType, date: String, quantity: String, note: String, nextDose: String): String? {
        val day = date(date)
        return when {
            date.isBlank() -> "Please enter the date."
            day == null -> "Date must be a valid date like 2026-10-30."
            type == RecordType.VACCINATION && note.isBlank() -> "Please enter the vaccine name."
            type == RecordType.DEATH && quantity.isBlank() -> "Please enter the number of deaths."
            type == RecordType.DEATH && positiveInt(quantity) == null -> "Deaths must be a whole number greater than 0."
            type == RecordType.PRODUCTION && quantity.isBlank() -> "Please enter the egg or litre count."
            type == RecordType.PRODUCTION && positiveDouble(quantity) == null -> "Count must be a number greater than 0."
            type == RecordType.VACCINATION && nextDose.isNotBlank() && date(nextDose) == null ->
                "Next dose date must be a valid date like 2026-11-20."
            type == RecordType.VACCINATION && nextDose.isNotBlank() && date(nextDose)!! < day ->
                "Next dose date cannot be before the vaccination date."
            else -> null
        }
    }
}
