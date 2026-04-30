package pl.parseus.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = APP_PACKAGE_NAME,
            includeInStartupProfile = false
        ) {
            pressHome()
            startActivityAndWait()

            waitForAsyncContent()

            switchToTab("Audio")
            scrollMainList()
            clickThroughCodecList()
            swipeToTab(Direction.LEFT) // Swipe to Video
            scrollMainList()
            clickThroughCodecList()
            swipeToTab(Direction.LEFT) // Swipe to DRM
            scrollMainList()
            clickThroughDrmList()

            testSearch()
            testHeaderNavigation()
            testSettingsRefresh()
        }
    }

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = APP_PACKAGE_NAME,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            waitForAsyncContent()
            device.waitForIdle()
        }
    }

}