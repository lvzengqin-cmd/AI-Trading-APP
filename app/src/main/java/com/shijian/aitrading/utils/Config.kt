// Config.kt - 配置文件
package com.shijian.aitrading.utils

object Config {
    // 服务器配置 - Render免费版
    const val SERVER_URL = "https://shijian-ai-backend.onrender.com"
    const val API_BASE = "$SERVER_URL/api"
    
    // 客服配置
    const val CUSTOMER_SERVICE_QQ = "1306353623"
    const val WEBSITE_URL = "https://www.yucebot.com"
    
    // 默认交易金额
    const val DEFAULT_TRADE_AMOUNT = "20"
    
    // 币安APP包名
    const val BINANCE_PACKAGE = "com.binance.dev"
    
    // 币安界面元素ID（基于你的宏脚本）
    object BinanceIds {
        const val TIME_30MIN = "30 分钟"
        const val AMOUNT_INPUT = "com.binance.dev:id/2131431205"
        const val BTN_BUY = "com.binance.dev:id/2131431697"  // 上涨/做多
        const val BTN_SELL = "com.binance.dev:id/2131431698" // 下跌/做空
        const val BTN_CONFIRM = "确认"
    }
    
    // 点击坐标（备用方案）
    object Coordinates {
        val TIME_30MIN = Pair(278f, 1016f)
        val AMOUNT_INPUT = Pair(267f, 1162f)
        val BTN_BUY = Pair(190f, 1359f)   // 做多按钮
        val BTN_SELL = Pair(530f, 1359f)  // 做空按钮
    }
}