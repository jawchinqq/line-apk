package com.example.linefloatingmonitor

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvMessages: TextView
    private lateinit var scrollView: ScrollView

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val msg = intent?.getStringExtra("message") ?: return
            tvMessages.append("\n$msg\n")
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_window, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            750,
            550,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 200
        }

        tvMessages = floatingView.findViewById(R.id.tv_messages)
        scrollView = floatingView.findViewById(R.id.scroll_view)
        val headerBar = floatingView.findViewById<RelativeLayout>(R.id.header_bar)
        val resizeHandle = floatingView.findViewById<TextView>(R.id.resize_handle)
        val btnClose = floatingView.findViewById<TextView>(R.id.btn_close)
        val btnClear = floatingView.findViewById<TextView>(R.id.btn_clear)

        btnClose.setOnClickListener { stopSelf() }
        btnClear.setOnClickListener { tvMessages.text = "" }

        // 頂部拖曳移動視窗
        headerBar.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        // 右下角拉動調整視窗大小
        resizeHandle.setOnTouchListener(object : View.OnTouchListener {
            private var initialWidth = 0
            private var initialHeight = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialWidth = params.width
                        initialHeight = params.height
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newWidth = initialWidth + (event.rawX - initialTouchX).toInt()
                        val newHeight = initialHeight + (event.rawY - initialTouchY).toInt()
                        if (newWidth > 350) params.width = newWidth
                        if (newHeight > 250) params.height = newHeight
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)

        val filter = IntentFilter("ACTION_NEW_LINE_MESSAGE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(messageReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(messageReceiver, filter)
        }
    }

    private fun startForegroundNotification() {
        val channelId = "floating_line_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LINE 懸浮窗常駐服務",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("LINE 群組懸浮窗執行中")
            .setContentText("正在即時接收 LINE 訊息...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
