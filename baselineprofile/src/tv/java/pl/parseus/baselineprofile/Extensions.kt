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
    val rowsList = onElements { viewIdResourceName == fullId("row_content") }
    for (i in 0 until minOf(rowsList.size, 3)) {
        with (rowsList[i]) {
            fling(Direction.RIGHT)
            fling(Direction.LEFT)

            onElements { viewIdResourceName == fullId("main_image") }.take(3).forEach {
                it.click()
                it.click()
                device.waitForIdle()
                onElementOrNull { viewIdResourceName == fullId("item_details_recycler_view") }
                    ?.fling(Direction.DOWN)

                device.pressBack()
            }
        }
    }
}

fun UiAutomatorTestScope.clickThroughDrmList() {
    // Already in DRM tab via testHeaderNavigation
    val rowsList = onElements { viewIdResourceName == fullId("row_content") }
    if (rowsList.isNotEmpty()) {
        with(rowsList.last()) {
             onElements { viewIdResourceName == fullId("main_image") }.forEach {
                it.click()
                device.waitForIdle()
                onElementOrNull { viewIdResourceName == fullId("item_details_recycler_view") }
                    ?.fling(Direction.DOWN)
                device.pressBack()
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
