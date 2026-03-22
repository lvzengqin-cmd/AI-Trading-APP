// TradingService.kt - 自动交易服务（核心）
package com.shijian.aitrading.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.shijian.aitrading.R
import com.shijian.aitrading.ui.MainActivity
import com.shijian.aitrading.utils.Config
import com.shijian.aitrading.utils.PreferenceManager
import kotlinx.coroutines.*

class TradingService : AccessibilityService() {
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var wakeLock: PowerManager.WakeLock? = null
    
    companion object {
        const val CHANNEL_ID = "trading_service"
        const val NOTIFICATION_ID = 1
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // 配置无障碍服务
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(Config.BINANCE_PACKAGE)
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                   AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        acquireWakeLock()
        
        Toast.makeText(this, "自动交易服务已启动", Toast.LENGTH_SHORT).show()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "EXECUTE_TRADE" -> {
                val type = intent.getStringExtra("trade_type") ?: return START_STICKY
                val amount = intent.getStringExtra("amount") ?: "20"
                scope.launch {
                    executeTrade(type, amount)
                }
            }
            "RECEIVE_SIGNAL" -> {
                val type = intent.getStringExtra("signal_type") ?: return START_STICKY
                val amount = PreferenceManager.getTradeAmount(this)
                scope.launch {
                    executeTrade(type, amount)
                }
            }
        }
        return START_STICKY
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 监听币安APP界面变化
    }
    
    override fun onInterrupt() {}
    
    // 执行交易（基于用户配置的坐标）
    private suspend fun executeTrade(type: String, amount: String) {
        try {
            // 获取配置
            val config = if (type == "buy") {
                PreferenceManager.getBuyConfig(this)
            } else {
                PreferenceManager.getSellConfig(this)
            }
            
            // 检查配置是否完成
            if (config.values.all { it.first == 0f && it.second == 0f }) {
                sendBroadcast(Intent("TRADE_ERROR").apply {
                    putExtra("error", "未配置交易坐标，请先进行配置")
                })
                return
            }
            
            // 1. 启动币安APP
            sendBroadcast(Intent("TRADE_LOG").apply {
                putExtra("message", "正在启动币安APP...")
            })
            launchBinance()
            delay(2000)
            
            // 2. 点击"事件合约"
            config["event"]?.let { (x, y) ->
                if (x != 0f && y != 0f) {
                    sendBroadcast(Intent("TRADE_LOG").apply {
                        putExtra("message", "点击事件合约位置 (${x.toInt()}, ${y.toInt()})")
                    })
                    clickByCoordinates(x, y)
                    delay(800)
                }
            }
            
            // 3. 点击"30分钟"
            config["time"]?.let { (x, y) ->
                if (x != 0f && y != 0f) {
                    sendBroadcast(Intent("TRADE_LOG").apply {
                        putExtra("message", "点击30分钟位置 (${x.toInt()}, ${y.toInt()})")
                    })
                    clickByCoordinates(x, y)
                    delay(800)
                }
            }
            
            // 4. 点击金额输入框
            config["amount"]?.let { (x, y) ->
                if (x != 0f && y != 0f) {
                    sendBroadcast(Intent("TRADE_LOG").apply {
                        putExtra("message", "点击金额输入框 (${x.toInt()}, ${y.toInt()})")
                    })
                    clickByCoordinates(x, y)
                    delay(800)
                }
            }
            
            // 5. 输入金额
            sendBroadcast(Intent("TRADE_LOG").apply {
                putExtra("message", "输入金额: $amount")
            })
            inputText(amount)
            delay(800)
            
            // 6. 点击做多/做空按钮
            config["button"]?.let { (x, y) ->
                if (x != 0f && y != 0f) {
                    sendBroadcast(Intent("TRADE_LOG").apply {
                        putExtra("message", "点击${if (type == "buy") "做多" else "做空"}按钮 (${x.toInt()}, ${y.toInt()})")
                    })
                    clickByCoordinates(x, y)
                    delay(800)
                }
            }
            
            // 7. 点击确认
            config["confirm"]?.let { (x, y) ->
                if (x != 0f && y != 0f) {
                    sendBroadcast(Intent("TRADE_LOG").apply {
                        putExtra("message", "点击确认按钮 (${x.toInt()}, ${y.toInt()})")
                    })
                    clickByCoordinates(x, y)
                    delay(500)
                }
            }
            
            // 发送成功通知
            sendBroadcast(Intent("TRADE_COMPLETED").apply {
                putExtra("type", type)
                putExtra("amount", amount)
            })
            
        } catch (e: Exception) {
            e.printStackTrace()
            sendBroadcast(Intent("TRADE_ERROR").apply {
                putExtra("error", e.message)
            })
        }
    }
    
    // 启动币安APP
    private fun launchBinance() {
        val intent = packageManager.getLaunchIntentForPackage(Config.BINANCE_PACKAGE)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }
    
    // 通过文本点击
    private fun clickByText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        return false
    }
    
    // 通过View ID点击
    private fun clickByViewId(viewId: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
        
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
        }
        return false
    }
    
    // 通过坐标点击
    private fun clickByCoordinates(x: Float, y: Float) {
        val path = android.graphics.Path().apply {
            moveTo(x, y)
        }
        val gesture = android.accessibilityservice.GestureDescription.Builder()
            .addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        dispatchGesture(gesture, null, null)
    }
    
    // 输入文本
    private fun inputText(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(Config.BinanceIds.AMOUNT_INPUT)
        
        for (node in nodes) {
            val arguments = android.os.Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            return
        }
    }
    
    // 保持屏幕常亮
    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "ShijianAI:TradingWakeLock"
        )
        wakeLock?.acquire(10*60*1000L) // 10分钟
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }
    
    // 创建通知渠道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动交易服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持自动交易服务在后台运行"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    // 创建前台通知
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("事件AI量化")
            .setContentText("自动交易服务运行中")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        scope.cancel()
    }
}