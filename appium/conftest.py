import os

import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options

APK = os.environ.get("APK", "app/build/outputs/apk/debug/app-debug.apk")


@pytest.fixture
def driver(request):
    """A fresh install of the debug APK on the emulator, driven through Appium's UiAutomator2 driver."""
    options = UiAutomator2Options()
    options.app = os.path.abspath(APK)
    options.app_package = "dev.nailkarimov.splitbill"
    options.app_activity = ".MainActivity"
    options.full_reset = False
    options.no_reset = False
    options.new_command_timeout = 120
    options.set_capability("appium:uiautomator2ServerInstallTimeout", 120_000)
    options.set_capability("appium:adbExecTimeout", 120_000)
    d = webdriver.Remote(os.environ.get("APPIUM_URL", "http://127.0.0.1:4723"), options=options)
    yield d
    os.makedirs("appium-results", exist_ok=True)
    d.save_screenshot(f"appium-results/{request.node.name}.png")
    d.quit()
