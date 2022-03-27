package com.lazymohan.letschat.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService: FirebaseMessagingService() {
  override fun onNewToken(p0: String) {
    super.onNewToken(p0)
    Log.d("FCM","Token : $p0")
  }

  override fun onMessageReceived(p0: RemoteMessage) {
    super.onMessageReceived(p0)
    Log.d("FCM","Message : ${p0.notification?.body}")
  }
}