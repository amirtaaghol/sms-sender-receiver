package com.example.sms_sender_receiver

import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    fun sendSMS(phoneNumber: String, message: String) {
        Log.d("SMS", "$phoneNumber and $message")
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("SMS", "Sent successfully")
        } catch (e: Exception) {
            Log.d("SMS", "Failed to send sms", e)
        }
    }

}