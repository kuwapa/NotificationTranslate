package com.pedalrhythm.notificationtranslate

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    private val ignoreMessages = arrayListOf(
        "Ongoing video call",
        "Ongoing voice call",
        "Ringing…",
        "Calling…",
        "Missed voice call",
        "Missed video call",
        "Incoming voice call",
        "Incoming video call",
        "Checking for new messages",
        "Call on hold",
        "\uD83D\uDCF7 Photo"
    )

    private val regexList = arrayListOf(
        "^\\d* missed voice calls$",
        "^\\d* missed video calls$",
        "^\\d* new messages$",
        "^\\d* messages from \\d* chats$",
        "^\uD83C\uDFA5 Video \\(\\d*:\\d*\\)$",
    )

    private fun matchesAnyRegex(string: String): Boolean {
        regexList.forEach {
            if (string.matches(it.toRegex())) {
                return true
            }
        }
        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == "com.whatsapp") {

            Log.d("notification_data", sbn.notification.extras.toString())
            val sender = sbn.notification.extras.getString("android.title")!!.toString()
            val content = sbn.notification.extras.getString("android.text")!!.toString()

            if (sender.matches("^Downloading \\d* videos$".toRegex())
                || sender == "WhatsApp"
                || sender == "Backup in progress"
            ) {
                return
            }

            if (ignoreMessages.contains(content) || matchesAnyRegex(content)) {
                return
            }

            CoroutineScope(Dispatchers.IO).launch { // do your background tasks here
                notificationDao.insert(
                    NotificationEntity(
                        sender, content, System.currentTimeMillis() / 1000
                    )
                )
            }
        }
    }
}
