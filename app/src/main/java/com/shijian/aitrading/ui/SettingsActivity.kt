// SettingsActivity.kt - 设置界面
package com.shijian.aitrading.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shijian.aitrading.R
import com.shijian.aitrading.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    private lateinit var rowConfigBuy: LinearLayout
    private lateinit var rowConfigSell: LinearLayout
    private lateinit var tvConfigBuyStatus: TextView
    private lateinit var tvConfigSellStatus: TextView
    private lateinit var etAmount: EditText
    private lateinit var switchAutoWake: Switch
    private lateinit var btnLogout: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initViews()
        updateUI()
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        rowConfigBuy = findViewById(R.id.row_config_buy)
        rowConfigSell = findViewById(R.id.row_config_sell)
        tvConfigBuyStatus = findViewById(R.id.tv_config_buy_status)
        tvConfigSellStatus = findViewById(R.id.tv_config_sell_status)
        etAmount = findViewById(R.id.et_amount)
        switchAutoWake = findViewById(R.id.switch_auto_wake)
        btnLogout = findViewById(R.id.btn_logout)
        
        // 返回按钮
        btnBack.setOnClickListener { finish() }
        
        // 配置点击
        rowConfigBuy.setOnClickListener {
            startConfig("buy")
        }
        
        rowConfigSell.setOnClickListener {
            startConfig("sell")
        }
        
        // 加载设置
        etAmount.setText(PreferenceManager.getTradeAmount(this))
        switchAutoWake.isChecked = PreferenceManager.getAutoWake(this)
        
        // 保存设置
        etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                PreferenceManager.setTradeAmount(this, etAmount.text.toString())
            }
        }
        
        switchAutoWake.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.setAutoWake(this, isChecked)
        }
        
        // 退出登录
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun updateUI() {
        // 显示配置状态
        val isBuyConfigured = PreferenceManager.isBuyConfigComplete(this)
        val isSellConfigured = PreferenceManager.isSellConfigComplete(this)
        
        tvConfigBuyStatus.text = if (isBuyConfigured) "已配置 ✅" else "未配置"
        tvConfigBuyStatus.setTextColor(if (isBuyConfigured) 
            getColor(android.R.color.holo_green_dark) else getColor(android.R.color.darker_gray))
        
        tvConfigSellStatus.text = if (isSellConfigured) "已配置 ✅" else "未配置"
        tvConfigSellStatus.setTextColor(if (isSellConfigured) 
            getColor(android.R.color.holo_green_dark) else getColor(android.R.color.darker_gray))
    }
    
    private fun startConfig(type: String) {
        val intent = Intent(this, ConfigActivity::class.java).apply {
            putExtra("trade_type", type)
        }
        startActivity(intent)
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出当前账户吗？")
            .setPositiveButton("确定") { _, _ ->
                logout()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun logout() {
        // 清除登录信息
        PreferenceManager.clear(this)
        
        // 跳转到登录界面
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Webhook", text)
        clipboard.setPrimaryClip(clip)
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
}