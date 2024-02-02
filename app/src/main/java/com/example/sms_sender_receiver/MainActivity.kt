package com.example.sms_sender_receiver

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProvider
import com.example.sms_sender_receiver.ui.theme.SmssenderreceiverTheme

class MainActivity : ComponentActivity() {

    private val TAG = "SMS_APP"
    private val SENT = "SMS_SENT"
    private val DELIVERED = "SMS_DELIVERED"
    private lateinit var viewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContent {
            SmssenderreceiverTheme {
                SMSApp(mainViewModel = viewModel)
            }
        }
        registerReceiver(sentReceiver, IntentFilter(SENT), RECEIVER_EXPORTED)
        registerReceiver(deliveredReceiver, IntentFilter(DELIVERED), RECEIVER_EXPORTED)

    }

    private val sentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "SMS was sent successfully")
                }

                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    Log.d(TAG, "SMS Generic failure")
                }

                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    Log.d(TAG, "SMS no service failure")
                }

                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    Log.d(TAG, "SMS radio off failure")
                }
            }
        }
    }

    private val deliveredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "SMS delivered successfully")
                }

                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "SMS deliver failed")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sentReceiver)
        unregisterReceiver(deliveredReceiver)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.d("intent" , intent.toString() )
        intent?.let {

            val number = it.getStringExtra("smsNumber")
            val message = it.getStringExtra("smsMessage")

            Log.d("SMS", "Number: $number, Message: $message")

            if(number != null && message != null) {
                showUserDialog(number, message)
            }
        }
    }

    private fun showUserDialog(sender: String, message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(sender)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SMSApp(mainViewModel: MainViewModel) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var phoneNumberState by remember { mutableStateOf("") }
            var messageState by remember { mutableStateOf("") }
            val maxPhoneNumberLength = 11
            val maxMessageLength = 240
            val blue = Color(0xff76a9ff)

            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { isGranted: Map<String, @JvmSuppressWildcards Boolean> ->

                if (isGranted.containsValue(false)) {
                    Log.d("SMS_Permissions", "denied")
                } else {
                    Log.d("SMS_Permissions", "granted")

                }

            }
            val permissions = arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.POST_NOTIFICATIONS
            )

            val phoneFocusRequester = FocusRequester()
            val messageFocusRequester = FocusRequester()

            Text(
                text = "Phone Number",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start,
                color = blue
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
                    )
                    .focusRequester(phoneFocusRequester),
                value = phoneNumberState,
                onValueChange = {
                    if (it.length <= maxPhoneNumberLength && it.isDigitsOnly()) phoneNumberState =
                        it
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                trailingIcon = {
                    if (phoneNumberState.isNotEmpty()) {
                        IconButton(onClick = { phoneNumberState = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
            Text(
                text = "${phoneNumberState.length} / $maxPhoneNumberLength",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.End,
                color = blue
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp)
            )

            Text(
                text = "Message",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start,
                color = blue
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
                    )
                    .height(240.dp)
                    .focusRequester(messageFocusRequester),
                value = messageState,
                onValueChange = { if (it.length <= maxMessageLength) messageState = it },
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    if (messageState.isNotEmpty()) {
                        IconButton(onClick = { messageState = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null
                            )
                        }
                    }
                }
            )
            Text(
                text = "${messageState.length} / $maxMessageLength",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.End,
                color = blue
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp)
            )

            Button(
                onClick = {
                    val notGrantedPermissions = permissions.filterNot { permission ->
                        ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    if (notGrantedPermissions.isNotEmpty()) {
                        launcher.launch(notGrantedPermissions.toTypedArray())
                    } else {
                        if (phoneNumberState.isEmpty() || phoneNumberState.length < 11 || !phoneNumberState.startsWith(
                                "09"
                            )
                        ) {
                            Toast.makeText(
                                context,
                                "Please fill in phone number correctly",
                                Toast.LENGTH_SHORT
                            ).show()
                            phoneFocusRequester.requestFocus()

                        } else if (messageState.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Please fill in message correctly",
                                Toast.LENGTH_SHORT
                            ).show()
                            messageFocusRequester.requestFocus()
                        } else {
                            mainViewModel.sendSMS(phoneNumberState, messageState)
                            Toast.makeText(context, "SMS sending...", Toast.LENGTH_SHORT).show()
                        }
                        Log.d("SMS_Permission", "We have all required permissions")
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(fontSize = 16.sp, text = "Send SMS")
            }
        }

    }
}
