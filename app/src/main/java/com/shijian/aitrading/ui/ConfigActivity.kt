// ConfigActivity.kt - 配置向导界面
package com.shijian.aitrading.ui

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shijian.aitrading.R
import com.shijian.aitrading.utils.Config
import com.shijian.aitrading.utils.PreferenceManager
import kotlinx.coroutines.*

class ConfigActivity : AppCompatActivity() {
    
    private var currentStep = 0
    private var tradeType = "buy" // buy or sell
    private var selectedAppPackage: String? = null
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        tradeType = intent.getStringExtra("trade_type") ?: "buy"
        
        // 检查是否有悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            showAppSelector()
        }
    }
    
    private fun requestOverlayPermission() {
        AlertDialog.Builder(this)
            .setTitle("需要悬浮窗权限")
            .setMessage("配置功能需要悬浮窗权限来引导您点击交易界面。请在设置中开启此权限。")
            .setPositiveButton("去开启") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            }
            .setNegativeButton("取消") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                showAppSelector()
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能使用配置功能", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun showAppSelector() {
        // 获取已安装的APP列表
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val apps = pm.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != packageName } // 排除自己
            .sortedBy { it.loadLabel(pm).toString() }
        
        if (apps.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("无法获取APP列表")
                .setMessage("请确保您的手机上已安装交易软件（如币安、HIBT等）")
                .setPositiveButton("确定") { _, _ -> finish() }
                .show()
            return
        }
        
        val appNames = apps.map { it.loadLabel(pm).toString() }.toTypedArray()
        val appPackages = apps.map { it.activityInfo.packageName }.toList()
        
        // 使用单选列表，更可靠
        AlertDialog.Builder(this)
            .setTitle("选择交易APP")
            .setItems(appNames) { _, which ->
                selectedAppPackage = appPackages[which]
                startConfig()
            }
            .setNegativeButton("取消") { _, _ ->
                finish()
            }
            .setCancelable(true)
            .show()
    }
    
    private fun startConfig() {
        currentStep = 0
        showStepDialog()
    }
    
    private fun showStepDialog() {
        if (currentStep >= Config.ConfigSteps.STEP_COMPLETE) {
            finishConfig()
            return
        }
        
        val stepName = Config.ConfigSteps.STEP_NAMES[currentStep]
        val typeText = if (tradeType == "buy") "做多" else "做空"
        
        AlertDialog.Builder(this)
            .setTitle("配置$typeText - 步骤 ${currentStep + 1}/6")
            .setMessage("请按以下步骤操作：\n\n1. 点击【确定】后，系统会打开您选择的交易APP\n2. 请在交易界面上找到并点击：\n   【$stepName】\n3. 点击后返回本界面\n\n提示：点击屏幕任意位置可记录当前坐标")
            .setPositiveButton("确定，打开交易APP") { _, _ ->
                launchAppAndShowOverlay()
            }
            .setNegativeButton("跳过此步骤") { _, _ ->
                currentStep++
                showStepDialog()
            }
            .setNeutralButton("重新选择APP") { _, _ ->
                showAppSelector()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun launchAppAndShowOverlay() {
        val packageName = selectedAppPackage
        if (packageName == null) {
            Toast.makeText(this, "请先选择交易APP", Toast.LENGTH_SHORT).show()
            showAppSelector()
            return
        }
        
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
            // 延迟显示悬浮窗
            window?.decorView?.postDelayed({
                showOverlay()
            }, 1500)
        } else {
            Toast.makeText(this, "无法启动该APP，请重新选择", Toast.LENGTH_LONG).show()
            showAppSelector()
        }
    }
    
    private fun showOverlay() {
        // 移除之前的悬浮窗
        removeOverlay()
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        
        overlayView = layoutInflater.inflate(R.layout.overlay_config, null)
        
        // 设置提示文字
        val stepName = Config.ConfigSteps.STEP_NAMES[currentStep]
        val typeText = if (tradeType == "buy") "做多" else "做空"
        overlayView?.findViewById<TextView>(R.id.tv_overlay_hint)?.text = 
            "【$typeText】步骤 ${currentStep + 1}/6\n请点击：$stepName"
        
        // 完成按钮
        overlayView?.findViewById<Button>(R.id.btn_overlay_done)?.setOnClickListener {
            // 使用屏幕中心作为默认坐标
            saveCoordinate(getScreenWidth() / 2f, getScreenHeight() / 2f)
            removeOverlay()
            currentStep++
            showStepDialog()
        }
        
        // 取消按钮
        overlayView?.findViewById<Button>(R.id.btn_overlay_cancel)?.setOnClickListener {
            removeOverlay()
            finish()
        }
        
        // 点击透明区域记录坐标
        overlayView?.findViewById<View>(R.id.overlay_touch_area)?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.rawX
                val y = event.rawY
                saveCoordinate(x, y)
                
                // 显示已记录的提示
                Toast.makeText(this, "✓ 已记录位置 (${x.toInt()}, ${y.toInt()})", Toast.LENGTH_SHORT).show()
            }
            true
        }
        
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            Toast.makeText(this, "显示悬浮窗失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getScreenWidth(): Float {
        return windowManager?.defaultDisplay?.let {
            val metrics = android.util.DisplayMetrics()
            it.getMetrics(metrics)
            metrics.widthPixels.toFloat()
        } ?: 1080f
    }
    
    private fun getScreenHeight(): Float {
        return windowManager?.defaultDisplay?.let {
            val metrics = android.util.DisplayMetrics()
            it.getMetrics(metrics)
            metrics.heightPixels.toFloat()
        } ?: 1920f
    }
    
    private fun saveCoordinate(x: Float, y: Float) {
        if (tradeType == "buy") {
            PreferenceManager.setBuyConfig(this, currentStep, x, y)
        } else {
            PreferenceManager.setSellConfig(this, currentStep, x, y)
        }
    }
    
    private fun removeOverlay() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
                overlayView = null
            }
        } catch (e: Exception) {
            // 忽略
        }
    }
    
    private fun finishConfig() {
        val isComplete = if (tradeType == "buy") {
            PreferenceManager.isBuyConfigComplete(this)
        } else {
            PreferenceManager.isSellConfigComplete(this)
        }
        
        if (isComplete) {
            AlertDialog.Builder(this)
                .setTitle("🎉 配置完成")
                .setMessage("${if (tradeType == "buy") "做多" else "做空"}配置已成功保存！\n\n现在您可以：\n1. 返回主界面点击测试按钮验证\n2. 开启自动交易功能")
                .setPositiveButton("确定") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("配置未完成")
                .setMessage("部分步骤被跳过，配置可能不完整。\n建议重新配置以获得最佳效果。")
                .setPositiveButton("重新配置") { _, _ ->
                    startConfig()
                }
                .setNegativeButton("稍后配置") { _, _ ->
                    finish()
                }
                .show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        scope.cancel()
    }
}