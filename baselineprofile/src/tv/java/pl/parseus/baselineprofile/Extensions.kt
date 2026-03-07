package pl.parseus.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.Until
import androidx.test.uiautomator.onElements

fun MacrobenchmarkScope.waitForAsyncContent() {
    device.wait(Until.hasObject(By.res(APP_PACKAGE_NAME, "container_list")), 5000L)
}

fun UiAutomatorTestScope.switchToTab(text: String) {}

fun UiAutomatorTestScope.clickThroughCodecList() {
    val rowsList = onElements { viewIdResourceName == fullId("row_content") }
    for (i in 0..2) {
        with (rowsList[i]) {
            fling(Direction.RIGHT)
            fling(Direction.LEFT)

            onElements { viewIdResourceName == fullId("main_image") }.forEach {
                it.click()
                it.click()
                device.waitForIdle()
                onElementOrNull { viewIdResourceName == fullId("full_codec_info_content") }
                    ?.fling(Direction.DOWN)

                device.pressBack()
            }
        }
    }
}

fun UiAutomatorTestScope.clickThroughDrmList() {}