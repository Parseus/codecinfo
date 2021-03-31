package com.parseus.codecinfo.utils

import android.os.Build
import java.lang.reflect.Method

val CAN_USE_REFLECTION_FOR_MCAPABILITIESINFO = Build.VERSION.SDK_INT < 29

fun disableApiBlacklistOnPie() {
    if (Build.VERSION.SDK_INT != 28) {
        return
    }

    val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
    val getDeclaredMethod = Class::class.java.getDeclaredMethod("getDeclaredMethod", String::class.java, arrayOf<Class<*>>()::class.java)
    val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
    val getRuntime = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
    val setHiddenApiExemptions = getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", arrayOf(arrayOf<String>()::class.java)) as Method
    val vmRuntime = getRuntime.invoke(null)
    setHiddenApiExemptions.invoke(vmRuntime, arrayOf("L"))
}