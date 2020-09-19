package com.parseus.codecinfo.memoryleakfixes

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.ContextWrapper
import android.os.Build
import android.os.Looper
import android.os.MessageQueue
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import java.lang.reflect.Field
import java.lang.reflect.Method

class IMMLeaks {

    class ReferenceCleaner(private val inputMethodManager: InputMethodManager,
                                 private val mHField: Field,
                                 private val mServedViewField: Field,
                                 private val finishInputLockedMethod: Method)
        : MessageQueue.IdleHandler, View.OnAttachStateChangeListener,
            ViewTreeObserver.OnGlobalFocusChangeListener {

        override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
            if (newFocus == null) {
                return
            }

            oldFocus?.removeOnAttachStateChangeListener(this)

            Looper.myQueue().removeIdleHandler(this)
            newFocus.addOnAttachStateChangeListener(this)
        }

        override fun onViewAttachedToWindow(v: View) {}

        override fun onViewDetachedFromWindow(v: View) {
            v.removeOnAttachStateChangeListener(this)
            Looper.myQueue().removeIdleHandler(this)
            Looper.myQueue().addIdleHandler(this)
        }

        override fun queueIdle(): Boolean {
            clearInputMethodManagerLeak()
            return false
        }

        private fun clearInputMethodManagerLeak() {
            try {
                val lock = mHField.get(inputMethodManager)!!
                // This is highly dependent on the InputMethodManager implementation.
                synchronized(lock) {
                    val servedView = mServedViewField.get(inputMethodManager) as? View
                    if (servedView != null) {
                        val servedViewAttached = servedView.windowVisibility != View.GONE
                        if (servedViewAttached) {
                            // The view held by the IMM was replaced without a global focus change.
                            // Let's make sure we get notified when that view detaches.

                            // Avoid double registration.
                            servedView.removeOnAttachStateChangeListener(this)
                            servedView.addOnAttachStateChangeListener(this)
                        } else {
                            // servedView is not attached. InputMethodManager is being stupid!
                            val activity = extractActivity(servedView.context)
                            if (activity == null || activity.window == null) {
                                // Unlikely case. Let's finish the input anyways.
                                finishInputLockedMethod.invoke(inputMethodManager)
                            } else {
                                val decorView = activity.window.peekDecorView()
                                val windowAttached = decorView.windowVisibility != View.GONE
                                if (!windowAttached) {
                                    finishInputLockedMethod.invoke(inputMethodManager)
                                } else {
                                    decorView.requestFocusFromTouch()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        private fun extractActivity(context: Context): Activity? {
            var currentContext = context
            while (true) when (currentContext) {
                is Application -> return null
                is Activity -> return currentContext
                is ContextWrapper -> {
                    val baseContext = currentContext.baseContext
                    // Prevent Stack Overflow.
                    if (baseContext == currentContext) {
                        return null
                    }
                    currentContext = baseContext
                }
                else -> return null
            }
        }

    }

    companion object {
        @SuppressLint("DiscouragedPrivateApi")
        fun fixFocusedViewLeak(application: Application) {
            // Can't use refelction on API >= 28.
            if (Build.VERSION.SDK_INT >= 28) {
                return
            }

            val inputMethodManager = application.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            val mServedViewField: Field
            val mHField: Field
            val finishInputLockedMethod: Method
            val focusInMethod: Method

            try {
                mServedViewField = InputMethodManager::class.java.getDeclaredField("mServedView")
                mServedViewField.isAccessible = true
                mHField = InputMethodManager::class.java.getDeclaredField("mH")
                mHField.isAccessible = true
                finishInputLockedMethod = InputMethodManager::class.java.getDeclaredMethod("finishInputLocked")
                finishInputLockedMethod.isAccessible = true
                focusInMethod = InputMethodManager::class.java.getDeclaredMethod("focusIn", View::class.java)
                focusInMethod.isAccessible = true
            } catch (e: Exception) {
                return
            }

            application.registerActivityLifecycleCallbacks(object : LifecycleCallbacksAdapter() {
                override fun onActivityStarted(activity: Activity) {
                    val cleaner = ReferenceCleaner(inputMethodManager, mHField, mServedViewField,
                            finishInputLockedMethod)
                    val rootView = activity.window.decorView.rootView
                    val viewTreeObserver = rootView.viewTreeObserver
                    viewTreeObserver.addOnGlobalFocusChangeListener(cleaner)
                }
            })
        }
    }

}