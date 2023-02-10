package com.pedalrhythm.notificationtranslate

import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Widgets
        val msgLogStatus = findViewById<TextView>(R.id.msg_log_status)
        val viewWALogBtn = findViewById<Button>(R.id.view_wa_log_btn)
        val viewSignalLogBtn = findViewById<Button>(R.id.view_signal_log_btn)
        val notificationListenerSwitch = findViewById<SwitchMaterial>(R.id.notification_listener_switch)
        val test = findViewById<LinearLayout>(R.id.test)

        // TextView
        msgLogStatus.text = getString(R.string.msg_log_status_str,
            if (File(this.filesDir, msgLogFileName).exists()
                && File(this.filesDir, signalMsgLogFileName).exists()) checkEmoji else crossEmoji)

        // Button
        // DRY
        viewWALogBtn.setOnClickListener {
            val intent = Intent(this, MsgLogViewerActivity::class.java)
            intent.putExtra("app", "whatsapp")
            startActivity(intent)
        }

        viewSignalLogBtn.setOnClickListener {
            val intent = Intent(this, MsgLogViewerActivity::class.java)
            intent.putExtra("app", "signal")
            startActivity(intent)
        }

//        notificationListenerSwitch.isChecked = isServiceRunning(NotificationListener::class.java)
        notificationListenerSwitch.isChecked = checkNotificationEnabled()
        notificationListenerSwitch.isClickable = false
        test.setOnClickListener {
            //https://stackoverflow.com/questions/17861979/accessing-android-notificationlistenerservice-settings
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }
    }

    private fun createBackups() {
        if (!File(filesDir, msgLogFileName).exists()) {
            if (!File(this.filesDir, msgLogFileName).createNewFile())
                Toast.makeText(applicationContext, getString(R.string.create_msg_log_failed),
                    Toast.LENGTH_SHORT).show()
        }

        if (!File(filesDir, signalMsgLogFileName).exists()) {
            if (!File(this.filesDir, signalMsgLogFileName).createNewFile())
                Toast.makeText(applicationContext, getString(R.string.create_msg_log_failed),
                    Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
        return (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
            .getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == service.name }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_toggle_theme -> {

                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //check notification access setting is enabled or not
    private fun checkNotificationEnabled(): Boolean {
        try {
            return Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
                .contains(packageName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}