package dev.nailkarimov.splitbill

/**
 * Splits a bill between people. Money is in whole cents (Long), never floating point.
 *
 * - the bill is 0.01 to 100 000.00, the tip 0 to 30 %, people 1 to 50;
 * - the tip is rounded half up to a cent;
 * - everyone pays the same, rounded **up** to a cent, so the bill is always covered; [Split.extra] is what is
 *   collected on top of the total because of that rounding (always less than one cent per person).
 */
object BillSplitter {
    const val MAX_BILL_CENTS = 10_000_000L
    const val MAX_TIP_PERCENT = 30
    const val MAX_PEOPLE = 50

    data class Split(val tipCents: Long, val totalCents: Long, val perPersonCents: Long, val extraCents: Long)

    sealed class Problem(val field: String, val message: String) {
        object BadBill : Problem("bill", "Enter a bill from 0.01 to 100000.00")
        object BadTip : Problem("tip", "Tip is 0 to 30 %")
        object BadPeople : Problem("people", "People: 1 to 50")
    }

    /** Parses "12", "12.5" or "12.50" (a comma works too) into cents; null if it is not an amount. */
    fun parseCents(text: String): Long? {
        val t = text.trim().replace(',', '.')
        val m = Regex("""(\d{1,9})(?:\.(\d{1,2}))?""").matchEntire(t) ?: return null
        val whole = m.groupValues[1].toLong()
        val frac = m.groupValues[2].padEnd(2, '0').ifEmpty { "00" }.toLong()
        return whole * 100 + frac
    }

    fun split(billText: String, tipText: String, peopleText: String): Result<Split> {
        val bill = parseCents(billText)
        if (bill == null || bill < 1 || bill > MAX_BILL_CENTS) return failure(Problem.BadBill)
        val tip = tipText.trim().toIntOrNull()
        if (tip == null || tip < 0 || tip > MAX_TIP_PERCENT) return failure(Problem.BadTip)
        val people = peopleText.trim().toIntOrNull()
        if (people == null || people < 1 || people > MAX_PEOPLE) return failure(Problem.BadPeople)
        return Result.success(split(bill, tip, people))
    }

    fun split(billCents: Long, tipPercent: Int, people: Int): Split {
        val tip = (billCents * tipPercent + 50) / 100 // half up
        val total = billCents + tip
        val each = (total + people - 1) / people // ceiling
        return Split(tip, total, each, each * people - total)
    }

    fun format(cents: Long): String = "%d.%02d".format(cents / 100, cents % 100)

    class Invalid(val problem: Problem) : IllegalArgumentException(problem.message)

    private fun failure(p: Problem): Result<Split> = Result.failure(Invalid(p))
}
