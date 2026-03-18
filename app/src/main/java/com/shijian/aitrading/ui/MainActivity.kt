// MainActivity.kt - 主界面
package com.shijian.aitrading.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shijian.aitrading.R
import com.shijian.aitrading.service.TradingService
import com.shijian.aitrading.ui.adapter.LogAdapter
import com.shijian.aitrading.ui.adapter.SignalAdapter
import com.shijian.aitrading.utils.Config
import com.shijian.aitrading.utils.HttpUtil
import com.shijian.aitrading.utils.PreferenceManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvStatus: TextView
    private lateinit var tvExpireDate: TextView
    private lateinit var tvDeviceId: TextView
    private lateinit var etAmount: EditText
    private lateinit var switchAutoTrade: Switch
    private lateinit var switchAutoWake: Switch
    private lateinit var btnTestBuy: Button
    private lateinit var btnTestSell: Button
    private lateinit var cardPermission: View
    private lateinit var rvSignals: RecyclerView
    private lateinit var rvLogs: RecyclerView
    private lateinit var btnGrantPermission: Button
    
    private val signals = mutableListOf<SignalRecord>()
    private val logs = mutableListOf<String>()
    private lateinit var signalAdapter: SignalAdapter
    private lateinit var logAdapter: LogAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    data class SignalRecord(
        val time: String,
        val type: String,
        val message: String
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        initRecyclerViews()
        loadUserInfo()
        checkPermissions()
        loadSignals()
    }
    
    private fun initViews() {
        tvStatus = findViewById(R.id.tv_status)
        tvExpireDate = findViewById(R.id.tv_expire_date)
        tvDeviceId = findViewById(R.id.tv_device_id)
        etAmount = findViewById(R.id.et_amount)
        switchAutoTrade = findViewById(R.id.switch_auto_trade)
        switchAutoWake = findViewById(R.id.switch_auto_wake)
        btnTestBuy = findViewById(R.id.btn_test_buy)
        btnTestSell = findViewById(R.id.btn_test_sell)
        cardPermission = findViewById(R.id.card_permission)
        rvSignals = findViewById(R.id.rv_signals)
        rvLogs = findViewById(R.id.rv_logs)
        btnGrantPermission = findViewById(R.id.btn_grant_permission)
        
        // 加载保存的设置
        etAmount.setText(PreferenceManager.getTradeAmount(this))
        switchAutoWake.isChecked = PreferenceManager.getAutoWake(this)
        
        // 显示设备ID
        val deviceId = PreferenceManager.getDeviceId(this)
        tvDeviceId.text = "设备ID: ${deviceId.takeLast(8)}... (点击复制)"
        tvDeviceId.setOnClickListener {
            copyToClipboard(deviceId)
            Toast.makeText(this, "设备ID已复制", Toast.LENGTH_SHORT).show()
        }
        
        // 权限按钮
        btnGrantPermission.setOnClickListener {
            openAccessibilitySettings()
        }
        
        // 自动交易开关
        switchAutoTrade.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (isAccessibilityEnabled()) {
                    startTradingService()
                } else {
                    switchAutoTrade.isChecked = false
                    showPermissionDialog()
                }
            } else {
                stopTradingService()
            }
        }
        
        // 自动亮屏开关
        switchAutoWake.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.setAutoWake(this, isChecked)
            if (isChecked) {
                requestIgnoreBatteryOptimizations()
            }
            addLog(if (isChecked) "已开启自动亮屏" else "已关闭自动亮屏")
        }
        
        // 测试按钮
        btnTestBuy.setOnClickListener { testTrade("buy") }
        btnTestSell.setOnClickListener { testTrade("sell") }
        
        // 保存金额设置
        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                PreferenceManager.setTradeAmount(this, etAmount.text.toString())
            }
        }
    }
    
    private fun initRecyclerViews() {
        signalAdapter = SignalAdapter(signals)
        rvSignals.layoutManager = LinearLayoutManager(this)
        rvSignals.adapter = signalAdapter
        
        logAdapter = LogAdapter(logs)
        rvLogs.layoutManager = LinearLayoutManager(this)
        rvLogs.adapter = logAdapter
    }
    
    private fun loadUserInfo() {
        scope.launch {
            try {
                val token = PreferenceManager.getToken(this@MainActivity) ?: return@launch
                val response = HttpUtil.getUserInfo(token)
                
                response?.let {
                    val json = JSONObject(it)
                    val expireDate = json.optString("expireDate")
                    val isExpired = json.optBoolean("isExpired")
                    
                    runOnUiThread {
                        if (isExpired) {
                            tvExpireDate.text = "会员状态: 已过期"
                            tvExpireDate.setTextColor(getColor(android.R.color.holo_red_dark))
                            switchAutoTrade.isEnabled = false
                        } else {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Date(expireDate))
                            tvExpireDate.text = "会员到期: $date"
                        }
                    }
                }
            } catch (e: Exception) {
                addLog("加载用户信息失败: ${e.message}")
            }
        }
    }
    
    private fun loadSignals() {
        scope.launch {
            try {
                val token = PreferenceManager.getToken(this@MainActivity) ?: return@launch
                val response = HttpUtil.getSignals(token)
                
                response?.let {
                    val json = JSONObject(it)
                    val array = json.optJSONArray("signals") ?: return@let
                    
                    signals.clear()
                    for (i in 0 until array.length()) {
                        val signal = array.getJSONObject(i)
                        signals.add(SignalRecord(
                            time = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                .format(Date(signal.optLong("timestamp"))),
                            type = if (signal.optString("type") == "buy") "多" else "空",
                            message = signal.optString("message")
                        ))
                    }
                    
                    runOnUiThread {
                        signalAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                addLog("加载信号失败: ${e.message}")
            }
        }
    }
    
    private fun checkPermissions() {
        cardPermission.visibility = if (isAccessibilityEnabled()) View.GONE else View.VISIBLE
    }
    
    private fun isAccessibilityEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.id.contains(packageName) }
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    private fun requestIgnoreBatteryOptimizations() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
    
    private fun startTradingService() {
        val intent = Intent(this, TradingService::class.java)
        startService(intent)
        tvStatus.text = "● 运行中"
        tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        addLog("🚀 自动交易服务已启动")
    }
    
    private fun stopTradingService() {
        val intent = Intent(this, TradingService::class.java)
        stopService(intent)
        tvStatus.text = "○ 已停止"
        tvStatus.setTextColor(getColor(android.R.color.darker_gray))
        addLog("🛑 自动交易服务已停止")
    }
    
    private fun testTrade(type: String) {
        if (!isAccessibilityEnabled()) {
            showPermissionDialog()
            return
        }
        
        val amount = etAmount.text.toString()
        addLog("🧪 测试: ${if (type == "buy") "做多" else "做空"} $amount USDT")
        
        // 执行交易
        val intent = Intent(this, TradingService::class.java).apply {
            action = "EXECUTE_TRADE"
            putExtra("trade_type", type)
            putExtra("amount", amount)
        }
        startService(intent)
    }
    
    fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logs.add(0, "[$time] $message")
        logAdapter.notifyItemInserted(0)
        rvLogs.scrollToPosition(0)
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Device ID", text))
    }
    
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("请开启无障碍权限才能使用自动交易功能")
            .setPositiveButton("去开启") { _, _ -> openAccessibilitySettings() }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_contact -> {
                showContactDialog()
                true
            }
            R.id.menu_website -> {
                openWebsite()
                true
            }
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showContactDialog() {
        AlertDialog.Builder(this)
            .setTitle("联系客服")
            .setMessage("QQ: ${Config.CUSTOMER_SERVICE_QQ}\n\n点击复制QQ号")
            .setPositiveButton("复制QQ") { _, _ ->
                copyToClipboard(Config.CUSTOMER_SERVICE_QQ)
                Toast.makeText(this, "QQ号已复制", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Config.WEBSITE_URL))
        startActivity(intent)
    }
    
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出吗？")
            .setPositiveButton("确定") { _, _ ->
                PreferenceManager.clear(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}