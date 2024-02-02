package com.example.sms_sender_receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "com.example.sms_sender_receiver.SmsReceiver"
    private val CHANNEL_ID = "SMS_CHANNEL"
    private var NOTIFICATION_ID = 123

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        try {
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<Any>?
                if (pdus != null) {
                    for (pdu in pdus) {
                        val currentMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                        val phoneNumber: String = currentMessage.displayOriginatingAddress
                        val message: String = currentMessage.displayMessageBody
                        Log.d(TAG, "Received SMS: $message from $phoneNumber")

                        createNotification(context, phoneNumber, message)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: $e", e)
        }
    }


    private fun createNotification(context: Context, sender: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("smsNumber", sender)
        intent.putExtra("smsMessage", message)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK


        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        Log.d("Intent extras", intent.extras.toString())


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // or ic_dialog_alert, or ic_dialog_info
            .setContentTitle("New SMS from $sender")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        // Create the NotificationChannel on API 26+
        val name = "SMS Channel"
        val descriptionText = "Channel for SMS notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
            NOTIFICATION_ID = Random.nextInt()
        }
    }
}
