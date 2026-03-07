package pl.parseus.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.uiAutomator
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
            uiAutomator {
                pressHome()
                startActivityAndWait()

                waitForAsyncContent()

                switchToTab("Audio")
                clickThroughCodecList()
                switchToTab("Video")
                clickThroughCodecList()
                switchToTab("DRM")
                clickThroughDrmList()
            }
        }
    }

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = APP_PACKAGE_NAME,
            includeInStartupProfile = true
        ) {
            uiAutomator {
                pressHome()
                startActivityAndWait()
                device.waitForIdle()
            }
        }
    }

}