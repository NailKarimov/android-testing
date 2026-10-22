package dev.nailkarimov.splitbill

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** The money rules on the JVM, no device: milliseconds per test. */
class BillSplitterTest {
    @Test
    fun `everyone pays the same, rounded up, and the bill is always covered`() {
        val s = BillSplitter.split(10_000, 0, 3) // 100.00 between 3
        assertEquals(3334, s.perPersonCents)
        assertEquals(2, s.extraCents)
        assertTrue(s.perPersonCents * 3 >= s.totalCents)
    }

    @Test
    fun `tip is rounded half up to a cent`() {
        assertEquals(13, BillSplitter.split(125, 10, 1).tipCents) // 12.5 cents → 13
        assertEquals(12, BillSplitter.split(124, 10, 1).tipCents) // 12.4 cents → 12
    }

    @Test
    fun `an even split has nothing extra`() {
        val s = BillSplitter.split(8_000, 25, 4)
        assertEquals(2_000, s.tipCents)
        assertEquals(10_000, s.totalCents)
        assertEquals(2_500, s.perPersonCents)
        assertEquals(0, s.extraCents)
    }

    @Test
    fun `the extra is always less than a cent per person`() {
        for (bill in listOf(1L, 99L, 1_001L, 33_333L, 10_000_000L)) {
            for (tip in 0..30 step 5) {
                for (people in 1..50) {
                    val s = BillSplitter.split(bill, tip, people)
                    assertTrue("$bill $tip $people", s.extraCents in 0 until people)
                    assertEquals(s.totalCents + s.extraCents, s.perPersonCents * people)
                }
            }
        }
    }

    @Test
    fun `amounts parse with a dot or a comma and at most two decimals`() {
        assertEquals(1250L, BillSplitter.parseCents("12.5"))
        assertEquals(1250L, BillSplitter.parseCents(" 12,50 "))
        assertEquals(1200L, BillSplitter.parseCents("12"))
        assertNull(BillSplitter.parseCents("12.505"))
        assertNull(BillSplitter.parseCents("-1"))
        assertNull(BillSplitter.parseCents("1e3"))
        assertNull(BillSplitter.parseCents(""))
    }

    @Test
    fun `format pads cents`() {
        assertEquals("0.05", BillSplitter.format(5))
        assertEquals("1234.50", BillSplitter.format(123_450))
    }
}

/** Boundaries of every input: inside passes, one step outside names the right field. */
@RunWith(Parameterized::class)
class BillSplitterBoundaryTest(
    private val bill: String,
    private val tip: String,
    private val people: String,
    private val badField: String?,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} / {1}% / {2} → {3}")
        fun cases() = listOf(
            arrayOf("0.01", "10", "2", null),
            arrayOf("0", "10", "2", "bill"),
            arrayOf("100000.00", "10", "2", null),
            arrayOf("100000.01", "10", "2", "bill"),
            arrayOf("abc", "10", "2", "bill"),
            arrayOf("50", "0", "2", null),
            arrayOf("50", "30", "2", null),
            arrayOf("50", "31", "2", "tip"),
            arrayOf("50", "-1", "2", "tip"),
            arrayOf("50", "", "2", "tip"),
            arrayOf("50", "10", "1", null),
            arrayOf("50", "10", "50", null),
            arrayOf("50", "10", "0", "people"),
            arrayOf("50", "10", "51", "people"),
            arrayOf("50", "10", "2.5", "people"),
        )
    }

    @Test
    fun validates() {
        val r = BillSplitter.split(bill, tip, people)
        if (badField == null) {
            assertTrue(r.exceptionOrNull()?.message ?: "", r.isSuccess)
        } else {
            assertEquals(badField, (r.exceptionOrNull() as BillSplitter.Invalid).problem.field)
        }
    }
}
