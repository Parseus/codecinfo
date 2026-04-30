package pl.parseus.baselineprofile

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.textAsString

fun UiAutomatorTestScope.waitForAsyncContent() {
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

    for (i in 0 until 3) {
        onElements { viewIdResourceName == fullId("simpleCodecRow") }.getOrNull(i)?.let {
            it.click()
            device.waitForIdle()

            val detailsList = onElement { viewIdResourceName == fullId("item_details_recycler_view") }
            detailsList.setGestureMargin(device.displayWidth / 5)
            detailsList.fling(Direction.DOWN)
            detailsList.fling(Direction.DOWN) // Deep scroll
            detailsList.fling(Direction.UP)

            // Remember not to press back on a dual-pane layout.
            if (dpWidth < 800) {
                device.pressBack()
                device.waitForIdle()
            }
        }
    }
}

fun UiAutomatorTestScope.clickThroughDrmList() {
    val metrics = InstrumentationRegistry.getInstrumentation().context.resources.displayMetrics
    val dpWidth = metrics.widthPixels / metrics.density

    val count = onElements { viewIdResourceName == fullId("simpleDrmRow") }.size
    for (i in 0 until count) {
        onElements { viewIdResourceName == fullId("simpleDrmRow") }.getOrNull(i)?.let {
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
                device.waitForIdle()
            }
        }
    }
}

fun UiAutomatorTestScope.testSearch() {
    onElement { viewIdResourceName == fullId("search_bar") }.click()
    device.waitForIdle()

    val searchInput = onElement { viewIdResourceName == fullId("search_view_edit_text") }
    searchInput.text = "avc"
    device.waitForIdle()

    onElements { viewIdResourceName == fullId("simpleCodecRow") }.firstOrNull()?.click()
    device.waitForIdle()
    
    val metrics = InstrumentationRegistry.getInstrumentation().context.resources.displayMetrics
    val dpWidth = metrics.widthPixels / metrics.density
    if (dpWidth < 800) {
        device.pressBack() // Exit details
    }
    
    onElement { viewIdResourceName == fullId("search_view") }.apply {
        onElement { contentDescription?.contains("Clear", true) == true || viewIdResourceName?.contains("clear", true) == true }.click()
    }
    device.pressBack() // Close search
}

fun UiAutomatorTestScope.scrollMainList() {
    val mainList = onElement { viewIdResourceName == fullId("simpleCodecListView") }
    mainList.fling(Direction.DOWN)
    mainList.fling(Direction.UP)
}

@Suppress("UnusedReceiverParameter")
fun UiAutomatorTestScope.testHeaderNavigation() {
    // Not used in mobile
}

fun UiAutomatorTestScope.swipeToTab(direction: Direction) {
    onElement { viewIdResourceName == fullId("pager") }.fling(direction)
    device.waitForIdle()
}

fun UiAutomatorTestScope.testSettingsRefresh() {
    onElementOrNull { contentDescription?.contains("Settings", true) == true }?.click() ?: return
    device.waitForIdle()

    // Toggle "Show HW Codecs Only"
    onElementOrNull { text?.contains("hardware", true) == true }?.click()
    device.waitForIdle()

    device.pressBack()
    device.wait(Until.hasObject(By.res(APP_PACKAGE_NAME, "simpleCodecListView")), 5000L)
}
