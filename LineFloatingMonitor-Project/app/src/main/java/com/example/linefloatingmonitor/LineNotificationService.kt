package com.example.linefloatingmonitor

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.text.SimpleDateFormat
import java.util.*

class LineNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.packageName != "jp.naver.line.android") return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString() ?: ""

        // 讀取使用者設定的群組關鍵字過濾
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val filterKeyword = prefs.getString("group_filter", "") ?: ""

        if (filterKeyword.isNotEmpty()) {
            val matchGroup = subText.contains(filterKeyword, ignoreCase = true) ||
                             title.contains(filterKeyword, ignoreCase = true)
            if (!matchGroup) return
        }

        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val displayMsg = if (subText.isNotEmpty()) {
            "[$time] [$subText]\n$title：$text"
        } else {
            "[$time] $title：$text"
        }

        val intent = Intent("ACTION_NEW_LINE_MESSAGE").apply {
            setPackage(packageName)
            putExtra("message", displayMsg)
        }
        sendBroadcast(intent)
    }
}
