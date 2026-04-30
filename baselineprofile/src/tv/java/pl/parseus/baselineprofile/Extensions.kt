package pl.parseus.baselineprofile

import android.view.KeyEvent
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.onElements

fun UiAutomatorTestScope.waitForAsyncContent() {
    device.wait(Until.hasObject(By.res(APP_PACKAGE_NAME, "container_list")), 5000L)
}

@Suppress("UnusedReceiverParameter", "unused")
fun UiAutomatorTestScope.switchToTab(text: String) {
    // Not used on TV
}

fun UiAutomatorTestScope.clickThroughCodecList() {
    val rowCount = onElements { viewIdResourceName == fullId("row_content") }.size
    for (i in 0 until minOf(rowCount, 3)) {
        onElements { viewIdResourceName == fullId("row_content") }.getOrNull(i)?.let { row ->
            row.fling(Direction.RIGHT)
            row.fling(Direction.LEFT)

            for (j in 0 until 3) {
                onElements { viewIdResourceName == fullId("main_image") }.getOrNull(j)?.let { item ->
                    item.click()
                    item.click()
                    device.waitForIdle()
                    onElementOrNull { viewIdResourceName == fullId("item_details_recycler_view") }
                        ?.fling(Direction.DOWN)

                    device.pressBack()
                    device.waitForIdle()
                }
            }
        }
    }
}

fun UiAutomatorTestScope.clickThroughDrmList() {
    // Already in DRM tab via testHeaderNavigation
    val rowCount = onElements { viewIdResourceName == fullId("row_content") }.size
    if (rowCount > 0) {
        val lastRowIndex = rowCount - 1
        val itemCount = onElements { viewIdResourceName == fullId("main_image") }.size
        for (i in 0 until itemCount) {
            onElements { viewIdResourceName == fullId("main_image") }.getOrNull(i)?.let { item ->
                item.click()
                device.waitForIdle()
                onElementOrNull { viewIdResourceName == fullId("item_details_recycler_view") }
                    ?.fling(Direction.DOWN)
                device.pressBack()
                device.waitForIdle()
            }
        }
    }
}

fun UiAutomatorTestScope.testSearch() {
    onElement { contentDescription?.contains("Search", true) == true }.click()
    device.waitForIdle()

    device.pressKeyCode(KeyEvent.KEYCODE_A)
    device.pressKeyCode(KeyEvent.KEYCODE_V)
    device.pressKeyCode(KeyEvent.KEYCODE_C)
    device.waitForIdle()

    onElements { viewIdResourceName == fullId("main_image") }.firstOrNull()?.click()
    device.waitForIdle()
    device.pressBack()
    device.pressBack()
}

fun UiAutomatorTestScope.testHeaderNavigation() {
    // Move to headers
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_LEFT)
    device.waitForIdle()
    
    // Move down to Video
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_DOWN)
    device.waitForIdle()
    
    // Move back to content
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_RIGHT)
    device.waitForIdle()
}

fun UiAutomatorTestScope.scrollMainList() {
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_DOWN)
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_DOWN)
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_UP)
    device.pressKeyCode(KeyEvent.KEYCODE_DPAD_UP)
}

@Suppress("UnusedReceiverParameter", "unused")
fun UiAutomatorTestScope.swipeToTab(direction: Direction) {
    // Not used on TV
}

@Suppress("UnusedReceiverParameter")
fun UiAutomatorTestScope.testSettingsRefresh() {
    // Not used on TV
}
