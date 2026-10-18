package com.razorpay

import android.util.Log

/**
 * Fallback stub for com.razorpay.Logger to prevent NoClassDefFoundError
 * when CoreInitializer or Razorpay classes run in the background.
 */
object Logger {
    private const val DEFAULT_TAG = "Razorpay"

    @JvmStatic
    fun v(tag: String?, message: String?) {
        Log.v(tag ?: DEFAULT_TAG, message ?: "")
    }

    @JvmStatic
    fun d(tag: String?, message: String?) {
        Log.d(tag ?: DEFAULT_TAG, message ?: "")
    }

    @JvmStatic
    fun i(tag: String?, message: String?) {
        Log.i(tag ?: DEFAULT_TAG, message ?: "")
    }

    @JvmStatic
    fun w(tag: String?, message: String?) {
        Log.w(tag ?: DEFAULT_TAG, message ?: "")
    }

    @JvmStatic
    fun w(tag: String?, message: String?, tr: Throwable?) {
        Log.w(tag ?: DEFAULT_TAG, message ?: "", tr)
    }

    @JvmStatic
    fun e(tag: String?, message: String?) {
        Log.e(tag ?: DEFAULT_TAG, message ?: "")
    }

    @JvmStatic
    fun e(tag: String?, message: String?, tr: Throwable?) {
        Log.e(tag ?: DEFAULT_TAG, message ?: "", tr)
    }

    @JvmStatic
    fun printStackTraceString(tr: Throwable?): String {
        return tr?.stackTraceToString() ?: ""
    }
}
