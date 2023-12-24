package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import org.hyperskill.stopwatch.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var isStart = false
    private var notify = false
    private var upperLimit: String = ""
    private var currentColor: Int = 0
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder : NotificationCompat.Builder
    private val ID = 393939
    private val CHANNEL_ID = "org.hyperskill"
    private val name = "Stopwatch"
    private val descriptionText = "Time exceeded"
    @RequiresApi(Build.VERSION_CODES.N)
    private val importance = NotificationManager.IMPORTANCE_HIGH

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(name)
            .setContentText(descriptionText)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pIntent)


        binding.startButton.setOnClickListener {
            if(!isStart){
                startTime = System.currentTimeMillis()
                handler.postDelayed(updateTime,1000)
                isStart = true
                binding.progressBar.visibility = View.VISIBLE
                binding.settingsButton.isEnabled = false
                currentColor = binding.textView.currentTextColor

            }
        }

        binding.resetButton.setOnClickListener {

            binding.textView.text = "00:00"
            handler.removeCallbacks(updateTime)
            startTime = 0L
            isStart = false
            binding.progressBar.visibility = View.GONE
            binding.settingsButton.isEnabled = true
            binding.textView.setTextColor(Color.GRAY)
            notify = false
        }

        binding.settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null, false)
            AlertDialog.Builder(this)
                .setTitle("Set upper limit in seconds")
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    upperLimit = editText.text.toString().ifEmpty { "" }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateTime)
    }

    private val updateTime: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun run() {
            val seconds = ((System.currentTimeMillis()-startTime)/1000)%60
            val minutes = ((System.currentTimeMillis()-startTime)/(1000*60))%60
            binding.textView.text = String.format(Locale.getDefault(), "%02d:%02d",  minutes, seconds)

            if(upperLimit!="" && !notify && upperLimit.toInt() > 0 && upperLimit.toInt() < seconds){
                binding.textView.setTextColor(Color.RED)
                val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = notificationBuilder.build()
                notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_ONLY_ALERT_ONCE
                mNotificationManager.notify(ID, notification)
                notify = true
            }

            val randomColor = Random.nextInt()
            binding.progressBar.indeterminateTintList = ColorStateList.valueOf(randomColor)
            handler.postDelayed(this, 1000)
        }
    }

}

