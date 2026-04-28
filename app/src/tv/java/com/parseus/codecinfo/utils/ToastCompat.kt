package com.parseus.codecinfo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import java.lang.reflect.Field
import java.lang.reflect.Modifier


class ToastCompat(context: Context) : Toast(context) {

    override fun show() {
        if (Build.VERSION.SDK_INT == 25) {
            tryToHack()
        }
        super.show()
    }

    private fun tryToHack() {
        try {
            val mTN = getFieldValue(this, "mTN")
            if (mTN != null) {
                var isSuccess = false

                // a hack to some device which use the code between android 6.0 and android 7.1.1
                val rawShowRunnable = getFieldValue(mTN, "mShow")
                if (rawShowRunnable != null && rawShowRunnable is Runnable) {
                    isSuccess = setFieldValue(mTN, "mShow", InternalRunnable(rawShowRunnable))
                }

                // hack to android 7.1.1, these cover 99% devices.
                if (!isSuccess) {
                    val rawHandler = getFieldValue(mTN, "mHandler")
                    if (rawHandler != null && rawHandler is Handler) {
                        setFieldValue(
                            rawHandler,
                            "mCallback",
                            InternalHandlerCallback(rawHandler)
                        )
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    private class InternalRunnable(private val runnable: Runnable) : Runnable {
        override fun run() {
            try {
                runnable.run()
            } catch (_: Throwable) {}
        }
    }

    private class InternalHandlerCallback(private val handler: Handler) : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            try {
                handler.handleMessage(msg)
            } catch (_: Throwable) {}
            return true
        }
    }

    companion object {

        @Throws(Resources.NotFoundException::class)
        fun makeText(context: Context, resId: Int, duration: Int): Toast {
            return makeText(context, context.resources.getText(resId), duration)
        }

        @Suppress("DEPRECATION")
        @SuppressLint("DiscouragedApi")
        fun makeText(context: Context, text: CharSequence, duration: Int): ToastCompat {
            val result = ToastCompat(context)

            val inflate =
                context.getSystemService(LayoutInflater::class.java)
            val resources = context.resources
            val v = inflate.inflate(
                resources.getIdentifier(
                    "transient_notification",
                    "layout",
                    "android"
                ), null
            )
            val tv = v.findViewById<TextView>(
                resources.getIdentifier(
                    "message",
                    "id",
                    "android"
                )
            )
            tv.text = text
            result.view = v
            result.setDuration(duration)
            return result
        }

        @SuppressLint("DiscouragedPrivateApi")
        private fun setFieldValue(
            obj: Any,
            fieldName: String,
            newFieldValue: Any
        ): Boolean {
            val field = getDeclaredField(obj, fieldName)
            if (field != null) {
                try {
                    val accessFlags = field.modifiers
                    if (Modifier.isFinal(accessFlags)) {
                        val modifiersField = Field::class.java.getDeclaredField("accessFlags")
                        modifiersField.isAccessible = true
                        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
                    }
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }
                    field.set(obj, newFieldValue)
                    return true
                } catch (_: Throwable) {}
            }
            return false
        }

        private fun getFieldValue(obj: Any, fieldName: String): Any? {
            val field = getDeclaredField(obj, fieldName)
            return getFieldValue(obj, field)
        }

        private fun getFieldValue(obj: Any?, field: Field?): Any? {
            if (field != null) {
                try {
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }
                    return field.get(obj)
                } catch (_: Throwable) {}
            }
            return null
        }

        private fun getDeclaredField(obj: Any, fieldName: String): Field? {
            var superClass: Class<*>? = obj.javaClass
            while (superClass != null && superClass != Any::class.java) {
                try {
                    return superClass.getDeclaredField(fieldName)
                } catch (_: NoSuchFieldException) {
                    superClass = superClass.getSuperclass()
                }
            }
            return null
        }

    }

}