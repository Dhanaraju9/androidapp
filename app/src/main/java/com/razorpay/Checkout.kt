package com.razorpay

import android.app.Activity
import android.content.Context
import org.json.JSONObject

interface PaymentResultListener {
    fun onPaymentSuccess(razorpayPaymentId: String?)
    fun onPaymentError(code: Int, response: String?)
}

class Checkout {
    companion object {
        @JvmStatic
        fun preload(context: Context) {}

        @JvmStatic
        fun clearUserData(context: Context) {}
    }

    fun setKeyID(key: String) {}

    fun open(activity: Activity, options: JSONObject) {
        if (activity is PaymentResultListener) {
            activity.runOnUiThread {
                val mockPaymentId = "pay_demo_${System.currentTimeMillis()}"
                activity.onPaymentSuccess(mockPaymentId)
            }
        }
    }
}
