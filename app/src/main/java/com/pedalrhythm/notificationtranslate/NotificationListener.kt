package com.pedalrhythm.notificationtranslate

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {

    private lateinit var notificationDao: NotificationDao

    override fun onListenerConnected() {
        super.onListenerConnected()
        Toast.makeText(this, "Notification Listener connected", Toast.LENGTH_SHORT).show()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
        notificationDao = db.notificationDao()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == "com.whatsapp") {

            Log.d("notification_data", sbn.notification.extras.toString())
            val sender = sbn.notification.extras.getString("android.title")
            val msg = sbn.notification.extras.getString("android.text")

            CoroutineScope(Dispatchers.IO).launch { // do your background tasks here
                notificationDao.insert(
                    NotificationEntity(
                        sender!!.toString(), msg!!.toString(), System.currentTimeMillis() / 1000
                    )
                )
            }
        }
    }
}
