package com.simplemobiletools.contacts.pro.services

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.simplemobiletools.contacts.pro.helpers.CALL_NUMBER
import com.simplemobiletools.contacts.pro.helpers.CALL_STATUS
import com.simplemobiletools.contacts.pro.helpers.DIALER_INTENT_FILTER
import com.simplemobiletools.contacts.pro.helpers.IS_INCOMING_CALL
import com.simplemobiletools.contacts.pro.objects.CallManager

@TargetApi(Build.VERSION_CODES.M)
class MyIncomingCallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callCallback)

        val handle = Uri.decode(call.details.handle.toString())
        val callerNumber = if (handle.contains("tel:")) {
            handle.substringAfter("tel:")
        } else {
            handle
        }

        Intent(this, DialerCallService::class.java).apply {
            putExtra(CALL_STATUS, call.state)
            putExtra(CALL_NUMBER, callerNumber)
            putExtra(IS_INCOMING_CALL, true)
            startService(this)
        }

        CallManager.updateCall(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        CallManager.updateCall(null)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            CallManager.updateCall(call)
            sendCallToActivity(call)
            if (state == Call.STATE_DISCONNECTED) {
                Intent(applicationContext, DialerCallService::class.java).apply {
                    stopService(this)
                }
            }
        }
    }

    private fun sendCallToActivity(call: Call) {
        Intent(DIALER_INTENT_FILTER).apply {
            putExtra(CALL_STATUS, call.state)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(this)
        }
    }
}
