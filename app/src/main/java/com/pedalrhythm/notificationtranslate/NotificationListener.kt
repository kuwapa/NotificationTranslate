package com.pedalrhythm.notificationtranslate

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast
import java.io.File
import java.text.DateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Toast.makeText(this, "Notification Listener connected", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        if (sbn?.packageName == "com.whatsapp") {

            val date =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date())
            val sender = sbn.notification.extras.getString("android.title")
            val msg = sbn.notification.extras.getString("android.text")

            File(this.filesDir, "msgLog.txt").appendText("$date | $sender: $msg\n")
        }
    }
}
