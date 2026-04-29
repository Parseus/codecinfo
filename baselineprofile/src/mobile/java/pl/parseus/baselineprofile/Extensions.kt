package pl.parseus.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.onElements
import androidx.test.uiautomator.textAsString

fun MacrobenchmarkScope.waitForAsyncContent() {
    device.wait(Until.hasObject(By.res(APP_PACKAGE_NAME, "simpleCodecListView")), 5000L)
    val codecList = device.findObject(By.res(APP_PACKAGE_NAME, "simpleCodecListView"))
    codecList.wait(Until.hasObject(By.res(APP_PACKAGE_NAME, "simpleCodecRow")), 5000L)
}

fun UiAutomatorTestScope.switchToTab(text: String) {
    onElement { textAsString() == text }.click()
    device.waitForIdle()
}

fun UiAutomatorTestScope.clickThroughCodecList() {
    val metrics = InstrumentationRegistry.getInstrumentation().context.resources.displayMetrics
    val dpWidth = metrics.widthPixels / metrics.density

    onElement { viewIdResourceName == fullId("simpleCodecListView") }.apply {
        onElements {
            viewIdResourceName == fullId("simpleCodecRow")
        }.take(3).forEach {
            it.click()
            device.waitForIdle()

            val detailsList = onElement { viewIdResourceName == fullId("item_details_recycler_view") }
            detailsList.setGestureMargin(device.displayWidth / 5)
            detailsList.fling(Direction.DOWN)

            // Remember not to press back on a dual-pane layout.
            if (dpWidth < 800) {
                device.pressBack()
            }
        }
    }
}

fun UiAutomatorTestScope.clickThroughDrmList() {
    val metrics = InstrumentationRegistry.getInstrumentation().context.resources.displayMetrics
    val dpWidth = metrics.widthPixels / metrics.density

    onElement { viewIdResourceName == fullId("simpleCodecListView") }.apply {
        onElements {
            viewIdResourceName == fullId("simpleDrmRow")
        }.forEach {
            it.click()
            device.waitForIdle()

            // Other DRMs don't have enough info to be scrollable, might as well save a bit of time.
            onElementOrNull { textAsString() == "Widevine CDM" }?.let {
                val detailsList = onElement { viewIdResourceName == fullId("item_details_recycler_view") }
                detailsList.setGestureMargin(device.displayWidth / 5)
                detailsList.fling(Direction.DOWN)
            }

            // Remember not to press back on a dual-pane layout.
            if (dpWidth < 800) {
                device.pressBack()
            }
        }
    }
}