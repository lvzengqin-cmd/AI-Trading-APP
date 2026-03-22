// LoginActivity.kt - 登录界面
package com.shijian.aitrading.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shijian.aitrading.R
import com.shijian.aitrading.utils.Config
import com.shijian.aitrading.utils.HttpUtil
import com.shijian.aitrading.utils.PreferenceManager
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvDeviceId: TextView
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查是否已登录
        if (PreferenceManager.getToken(this) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_login)
        
        initViews()
        showDeviceInfo()
    }
    
    private fun initViews() {
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progress_bar)
        tvDeviceId = findViewById(R.id.tv_device_id)
        
        btnLogin.setOnClickListener { login() }
        btnRegister.setOnClickListener { showRegisterDialog() }
    }
    
    private fun showDeviceInfo() {
        val deviceId = PreferenceManager.getDeviceId(this)
        tvDeviceId.text = "设备ID: ${deviceId.takeLast(8)}...\n(联系客服开通会员)"
        tvDeviceId.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Device ID", deviceId))
            Toast.makeText(this, "设备ID已复制", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun login() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Demo模式 - 无需服务器
        if (Config.DEMO_MODE) {
            if (username == Config.DEMO_USERNAME && password == Config.DEMO_PASSWORD) {
                // Demo登录成功
                PreferenceManager.setToken(this, "demo_token_" + System.currentTimeMillis())
                PreferenceManager.setUsername(this, username)
                Toast.makeText(this, "Demo模式登录成功！", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            } else {
                showError("Demo账号: ${Config.DEMO_USERNAME}, 密码: ${Config.DEMO_PASSWORD}")
                return
            }
        }
        
        showLoading(true)
        
        scope.launch {
            try {
                val deviceId = PreferenceManager.getDeviceId(this@LoginActivity)
                val response = HttpUtil.login(username, password, deviceId)
                
                response?.let {
                    val json = JSONObject(it)
                    if (json.optBoolean("success")) {
                        val token = json.optString("token")
                        PreferenceManager.setToken(this@LoginActivity, token)
                        PreferenceManager.setUsername(this@LoginActivity, username)
                        
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        showError(json.optString("error", "登录失败"))
                    }
                } ?: showError("网络错误，请检查服务器配置")
            } catch (e: Exception) {
                showError("登录失败: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showRegisterDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_register, null)
        val etRegUsername = view.findViewById<EditText>(R.id.et_reg_username)
        val etRegPassword = view.findViewById<EditText>(R.id.et_reg_password)
        val etRegPassword2 = view.findViewById<EditText>(R.id.et_reg_password2)
        
        AlertDialog.Builder(this)
            .setTitle("注册账号")
            .setView(view)
            .setPositiveButton("注册") { _, _ ->
                val username = etRegUsername.text.toString().trim()
                val password = etRegPassword.text.toString().trim()
                val password2 = etRegPassword2.text.toString().trim()
                
                if (validateRegister(username, password, password2)) {
                    register(username, password)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun validateRegister(username: String, password: String, password2: String): Boolean {
        when {
            username.isEmpty() -> {
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show()
                return false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
                return false
            }
            password != password2 -> {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show()
                return false
            }
            password.length < 6 -> {
                Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }
    
    private fun register(username: String, password: String) {
        // Demo模式 - 注册功能提示
        if (Config.DEMO_MODE) {
            Toast.makeText(this, "Demo模式：请使用账号 ${Config.DEMO_USERNAME} 密码 ${Config.DEMO_PASSWORD} 登录", Toast.LENGTH_LONG).show()
            etUsername.setText(Config.DEMO_USERNAME)
            etPassword.setText(Config.DEMO_PASSWORD)
            return
        }
        
        showLoading(true)
        
        scope.launch {
            try {
                val deviceId = PreferenceManager.getDeviceId(this@LoginActivity)
                val response = HttpUtil.register(username, password, deviceId)
                
                response?.let {
                    val json = JSONObject(it)
                    if (json.optBoolean("success")) {
                        Toast.makeText(
                            this@LoginActivity,
                            "注册成功！请登录",
                            Toast.LENGTH_LONG
                        ).show()
                        etUsername.setText(username)
                    } else {
                        showError(json.optString("error", "注册失败"))
                    }
                } ?: showError("网络错误，请检查服务器配置")
            } catch (e: Exception) {
                showError("注册失败: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnRegister.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}