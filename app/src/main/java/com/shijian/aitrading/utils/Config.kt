// Config.kt - 应用配置
package com.shijian.aitrading.utils

object Config {
    // ======== API配置 ========
    // 后端服务地址
    const val BASE_URL = "https://ceyishijian-ai-backend.onrender.com"
    
    // API 端点
    const val API_REGISTER = "/api/register"
    const val API_LOGIN = "/api/login"
    const val API_USER_INFO = "/api/user/info"
    const val API_SIGNALS = "/api/signals"
    
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