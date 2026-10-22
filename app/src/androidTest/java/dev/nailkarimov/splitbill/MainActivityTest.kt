package dev.nailkarimov.splitbill

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The screen on a real (emulated) device. Espresso waits for the UI thread to be idle before every check. */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    private fun fill(bill: String, tip: String, people: String) {
        onView(withId(R.id.bill)).perform(replaceText(bill))
        onView(withId(R.id.tip)).perform(replaceText(tip))
        onView(withId(R.id.people)).perform(replaceText(people), closeSoftKeyboard())
    }

    @Test
    fun splitsTheBill() {
        fill("84.50", "10", "3")
        onView(withId(R.id.split)).perform(click())
        // 84.50 + 8.45 tip = 92.95; 92.95 / 3 = 30.983… → 30.99 each, 0.02 extra
        onView(withId(R.id.result)).check(matches(withText(containsString("Each pays 30.99"))))
        onView(withId(R.id.result)).check(matches(withText(containsString("Tip 8.45 · total 92.95"))))
        onView(withId(R.id.result)).check(matches(withText(containsString("collects 0.02 extra"))))
        onView(withId(R.id.error)).check(matches(withText("")))
    }

    @Test
    fun anEvenSplitSaysNothingAboutExtra() {
        fill("80", "25", "4")
        onView(withId(R.id.split)).perform(click())
        onView(withId(R.id.result)).check(matches(withText("Each pays 25.00\nTip 20.00 · total 100.00")))
    }

    @Test
    fun theKeyboardsDoneKeySplitsToo() {
        onView(withId(R.id.bill)).perform(replaceText("10"))
        onView(withId(R.id.people)).perform(replaceText("2"), pressImeActionButton())
        onView(withId(R.id.result)).check(matches(withText(containsString("Each pays 5.50"))))
    }

    @Test
    fun aBadTipIsExplained_andTheOldResultIsCleared() {
        fill("50", "10", "2")
        onView(withId(R.id.split)).perform(click())
        onView(withId(R.id.tip)).perform(replaceText("31"), closeSoftKeyboard())
        onView(withId(R.id.split)).perform(click())
        onView(withId(R.id.error)).check(matches(withText("Tip is 0 to 30 %")))
        onView(withId(R.id.result)).check(matches(withText("")))
    }

    @Test
    fun anEmptyBillIsExplained() {
        fill("", "10", "2")
        onView(withId(R.id.split)).perform(click())
        onView(withId(R.id.error)).check(matches(withText("Enter a bill from 0.01 to 100000.00")))
    }

    @Test
    fun plusAndMinusStayWithinOneToFifty() {
        onView(withId(R.id.people)).perform(replaceText("1"), closeSoftKeyboard())
        onView(withContentDescription("Fewer people")).perform(click())
        onView(withId(R.id.people)).check(matches(withText("1")))
        onView(withId(R.id.people)).perform(replaceText("50"), closeSoftKeyboard())
        onView(withContentDescription("More people")).perform(click())
        onView(withId(R.id.people)).check(matches(withText("50")))
        onView(withContentDescription("Fewer people")).perform(click())
        onView(withId(R.id.people)).check(matches(withText("49")))
    }

    @Test
    fun theResultSurvivesRecreation() {
        fill("84.50", "10", "3")
        onView(withId(R.id.split)).perform(click())
        rule.scenario.recreate() // what a rotation or a dark-mode switch does to the activity
        onView(withId(R.id.result)).check(matches(withText(containsString("Each pays 30.99"))))
    }
}
