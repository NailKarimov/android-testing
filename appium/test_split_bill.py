"""Black-box tests through Appium: the installed APK, driven from outside the app process like a user.

Espresso (app/src/androidTest) runs inside the app and knows its views; Appium knows only what the screen exposes —
resource ids, text, content descriptions — which is also what accessibility services see.
"""

from appium.webdriver.common.appiumby import AppiumBy

PKG = "dev.nailkarimov.splitbill:id/"


def by_id(driver, name):
    return driver.find_element(AppiumBy.ID, PKG + name)


def scrolled_to(driver, name):
    """UiAutomator only reports views on screen; in landscape the result is below the fold and has to be scrolled to."""
    return driver.find_element(
        AppiumBy.ANDROID_UIAUTOMATOR,
        "new UiScrollable(new UiSelector().scrollable(true))"
        f'.scrollIntoView(new UiSelector().resourceId("{PKG}{name}"))',
    )


def fill(driver, bill, tip, people):
    for field, value in (("bill", bill), ("tip", tip), ("people", people)):
        el = by_id(driver, field)
        el.clear()
        el.send_keys(value)
    if driver.is_keyboard_shown():
        driver.hide_keyboard()


def test_splits_a_bill(driver):
    fill(driver, "84.50", "10", "3")
    by_id(driver, "split").click()
    text = by_id(driver, "result").text
    assert "Each pays 30.99" in text
    assert "collects 0.02 extra" in text


def test_explains_a_bad_people_count(driver):
    fill(driver, "50", "10", "0")
    by_id(driver, "split").click()
    assert by_id(driver, "error").text == "People: 1 to 50"
    assert by_id(driver, "result").text == ""


def test_steppers_are_found_by_their_accessible_names(driver):
    by_id(driver, "people").clear()
    by_id(driver, "people").send_keys("5")
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "More people").click()
    assert by_id(driver, "people").text == "6"
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Fewer people").click()
    driver.find_element(AppiumBy.ACCESSIBILITY_ID, "Fewer people").click()
    assert by_id(driver, "people").text == "4"


def test_result_survives_rotation(driver):
    fill(driver, "84.50", "10", "3")
    by_id(driver, "split").click()
    driver.orientation = "LANDSCAPE"
    try:
        assert "Each pays 30.99" in scrolled_to(driver, "result").text
    finally:
        driver.orientation = "PORTRAIT"


def test_every_input_has_a_visible_label(driver):
    labels = [e.text for e in driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.TextView")]
    for label in ("Bill", "Tip, %", "People"):
        assert label in labels
