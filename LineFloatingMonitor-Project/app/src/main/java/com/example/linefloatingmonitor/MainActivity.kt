package com.example.linefloatingmonitor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val etFilter = findViewById<EditText>(R.id.et_group_filter)
        etFilter.setText(prefs.getString("group_filter", ""))

        // 1. 跳轉懸浮窗權限設定
        findViewById<Button>(R.id.btn_overlay_perm).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "已擁有懸浮窗權限！", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. 跳轉通知讀取權限設定
        findViewById<Button>(R.id.btn_notify_perm).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, "請在列表中找到「LINE群組懸浮窗」並開啟", Toast.LENGTH_LONG).show()
        }

        // 3. 儲存群組過濾關鍵字
        findViewById<Button>(R.id.btn_save_filter).setOnClickListener {
            val keyword = etFilter.text.toString().trim()
            prefs.edit().putString("group_filter", keyword).apply()
            val msg = if (keyword.isEmpty()) "已設為：顯示所有 LINE 訊息" else "已設為只顯示包含「$keyword」的群組"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 4. 啟動懸浮視窗
        findViewById<Button>(R.id.btn_start_floating).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "請先點擊按鈕 1 開啟懸浮窗權限！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val serviceIntent = Intent(this, FloatingWindowService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "浮動視窗已啟動！可回到桌面查看", Toast.LENGTH_SHORT).show()
        }
    }
}
