package com.pedalrhythm.notificationtranslate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.pedalrhythm.notificationtranslate.databinding.ActivityMainBinding
import com.pedalrhythm.notificationtranslate.databinding.MsgLogItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationDao: NotificationDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()
        notificationDao = db.notificationDao()

        binding.recyclerView.addItemDecoration(VerticalSpaceItemDecoration(8.toPx.toInt()))
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        refreshData()

        binding.deleteAllButton.setOnClickListener {
            showMaterialDialog(
                "Delete all notifications",
                "Are you sure you want to delete all saved notifications?",
                { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        notificationDao.deleteAll()
                        withContext(Dispatchers.Main) {
                            refreshData()
                            Toast.makeText(this@MainActivity, "All notifications deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                "Delete All",
                { dialog, _ -> dialog.dismiss() },
                "Cancel",
                false
            )
        }
    }

    private fun refreshData() {
        CoroutineScope(Dispatchers.IO).launch {
            val notifications: List<NotificationEntity> = notificationDao.getAll()
            Log.d("data", notifications.toString())

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false

                binding.recyclerView.adapter = NotificationsAdapter(this@MainActivity, notifications)
                binding.emptyListTextView.visibility =
                    if (notifications.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isNotificationsAccessEnabled()) {
            showMaterialDialog(
                "Enable Notification Access",
                "This app needs access to the notifications",
                { _, _ ->
                    //https://stackoverflow.com/questions/17861979/accessing-android-notificationlistenerservice-settings
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    startActivity(intent)
                },
                "Enable",
                null,
                "",
                false
            )
        }
    }

    private fun toggleTheme() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}

class NotificationsAdapter(
    private val context: Context,
    private val notificationsList: List<NotificationEntity>
) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MsgLogItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MsgLogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = notificationsList[position]
        holder.binding.fromTextView.text = msg.sender
        holder.binding.contentTextView.text = msg.content
        holder.binding.timeTextView.text = dateEpochToString(msg.time)

        holder.binding.translateButton.setOnClickListener {
            try {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, msg.content)
                intent.component = ComponentName(
                    "com.google.android.apps.translate",
                    "com.google.android.apps.translate.TranslateActivity"
                )
                startActivity(context, intent, null)

            } catch (e: ActivityNotFoundException) {
                (context as Activity).showMaterialDialog(
                    "Install Google Translate",
                    "Install the Google translate app for text translation",
                    { _, _ ->
                        val packageName = "com.google.android.apps.translate"
                        try {
                            Log.d("", "onOpenPlayStore $packageName")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
                            startActivity(context, intent, null)
                        } catch (e: ActivityNotFoundException) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}"))
                            startActivity(context, intent, null)
                        }
                    },
                    "Install",
                    null,
                    "",
                    false
                )
            }
        }
    }

    override fun getItemCount(): Int = notificationsList.size
}

@SuppressLint("SimpleDateFormat")
fun dateEpochToString(time: Long): String {
    val formatter = SimpleDateFormat("h:mm a, d MMM")
    return formatter.format(time * 1000)
}