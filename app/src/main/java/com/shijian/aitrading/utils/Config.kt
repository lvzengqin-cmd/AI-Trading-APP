// Config.kt - 应用配置
package com.shijian.aitrading.utils

object Config {
    // ======== API配置 ========
    // 后端服务地址
    const val BASE_URL = "https://ceyishijian-ai-backend.onrender.com"
    const val API_BASE = BASE_URL  // 兼容旧代码
    
    // API 端点
    const val API_REGISTER = "/api/register"
    const val API_LOGIN = "/api/login"
    const val API_USER_INFO = "/api/user/info"
    const val API_SIGNALS = "/api/signals"
    
    // ======== 演示模式配置 ========
    const val DEMO_MODE = false
    const val DEMO_USERNAME = "demo"
    const val DEMO_PASSWORD = "demo123"
    
    // ======== 交易APP配置 ========
    const val BINANCE_PACKAGE = "com.binance.dev"
    
    // 币安APP的界面元素ID
    object BinanceIds {
        const val EVENT_CONTRACT_BUTTON = "com.binance.dev:id/event_contract"
        const val TIME_30M_BUTTON = "com.binance.dev:id/time_30m"
        const val AMOUNT_INPUT = "com.binance.dev:id/amount_input"
        const val BUY_BUTTON = "com.binance.dev:id/buy_button"
        const val SELL_BUTTON = "com.binance.dev:id/sell_button"
        const val CONFIRM_BUTTON = "com.binance.dev:id/confirm_button"
    }
    
    // ======== Webhook配置（由后端自动生成，用户不可见）========
    // 注意：Webhook ID 不再存储在本地，而是完全由后端管理
    // 用户只需要登录，系统自动连接
    
    // ======== 客服配置 ========
    const val CUSTOMER_SERVICE_QQ = "23558335"
    
    // ======== 配置步骤（用于引导用户配置交易APP）========
    object ConfigSteps {
        const val STEP_EVENT_CONTRACT = 0
        const val STEP_TIME_30M = 1
        const val STEP_AMOUNT_INPUT = 2
        const val STEP_ENTER_AMOUNT = 3
        const val STEP_TRADE_BUTTON = 4
        const val STEP_CONFIRM = 5
        const val STEP_COMPLETE = 6
        
        val STEP_NAMES = arrayOf(
            "事件合约按钮",
            "30分钟选项",
            "金额输入框",
            "输入金额",
            "做多/做空按钮",
            "确认按钮"
        )
    }
}