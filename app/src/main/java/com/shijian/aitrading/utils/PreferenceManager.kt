// PreferenceManager.kt - 本地存储管理
package com.shijian.aitrading.utils

import android.content.Context
import android.provider.Settings
import java.util.*

object PreferenceManager {
    private const val PREF_NAME = "shijian_ai_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USERNAME = "username"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_TRADE_AMOUNT = "trade_amount"
    private const val KEY_AUTO_WAKE = "auto_wake"
    
    // 配置位置坐标 - 做多
    private const val KEY_BUY_EVENT_X = "buy_event_x"
    private const val KEY_BUY_EVENT_Y = "buy_event_y"
    private const val KEY_BUY_TIME_X = "buy_time_x"
    private const val KEY_BUY_TIME_Y = "buy_time_y"
    private const val KEY_BUY_AMOUNT_X = "buy_amount_x"
    private const val KEY_BUY_AMOUNT_Y = "buy_amount_y"
    private const val KEY_BUY_BUTTON_X = "buy_button_x"
    private const val KEY_BUY_BUTTON_Y = "buy_button_y"
    private const val KEY_BUY_CONFIRM_X = "buy_confirm_x"
    private const val KEY_BUY_CONFIRM_Y = "buy_confirm_y"
    
    // 配置位置坐标 - 做空
    private const val KEY_SELL_EVENT_X = "sell_event_x"
    private const val KEY_SELL_EVENT_Y = "sell_event_y"
    private const val KEY_SELL_TIME_X = "sell_time_x"
    private const val KEY_SELL_TIME_Y = "sell_time_y"
    private const val KEY_SELL_AMOUNT_X = "sell_amount_x"
    private const val KEY_SELL_AMOUNT_Y = "sell_amount_y"
    private const val KEY_SELL_BUTTON_X = "sell_button_x"
    private const val KEY_SELL_BUTTON_Y = "sell_button_y"
    private const val KEY_SELL_CONFIRM_X = "sell_confirm_x"
    private const val KEY_SELL_CONFIRM_Y = "sell_confirm_y"

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    fun setToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_TOKEN, token).apply()
    }

    fun getUsername(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USERNAME, null)
    }

    fun setUsername(context: Context, username: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USERNAME, username).apply()
    }

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId == null) {
            deviceId = generateDeviceId(context)
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        
        return deviceId
    }

    private fun generateDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
    }

    fun getTradeAmount(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRADE_AMOUNT, "20") ?: "20"
    }

    fun setTradeAmount(context: Context, amount: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_TRADE_AMOUNT, amount).apply()
    }

    fun getAutoWake(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_WAKE, true)
    }

    fun setAutoWake(context: Context, autoWake: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_WAKE, autoWake).apply()
    }
    
    // 保存配置坐标 - 做多
    fun setBuyConfig(context: Context, step: Int, x: Float, y: Float) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        when (step) {
            0 -> { prefs.putFloat(KEY_BUY_EVENT_X, x); prefs.putFloat(KEY_BUY_EVENT_Y, y) }
            1 -> { prefs.putFloat(KEY_BUY_TIME_X, x); prefs.putFloat(KEY_BUY_TIME_Y, y) }
            2 -> { prefs.putFloat(KEY_BUY_AMOUNT_X, x); prefs.putFloat(KEY_BUY_AMOUNT_Y, y) }
            4 -> { prefs.putFloat(KEY_BUY_BUTTON_X, x); prefs.putFloat(KEY_BUY_BUTTON_Y, y) }
            5 -> { prefs.putFloat(KEY_BUY_CONFIRM_X, x); prefs.putFloat(KEY_BUY_CONFIRM_Y, y) }
        }
        prefs.apply()
    }
    
    // 保存配置坐标 - 做空
    fun setSellConfig(context: Context, step: Int, x: Float, y: Float) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        when (step) {
            0 -> { prefs.putFloat(KEY_SELL_EVENT_X, x); prefs.putFloat(KEY_SELL_EVENT_Y, y) }
            1 -> { prefs.putFloat(KEY_SELL_TIME_X, x); prefs.putFloat(KEY_SELL_TIME_Y, y) }
            2 -> { prefs.putFloat(KEY_SELL_AMOUNT_X, x); prefs.putFloat(KEY_SELL_AMOUNT_Y, y) }
            4 -> { prefs.putFloat(KEY_SELL_BUTTON_X, x); prefs.putFloat(KEY_SELL_BUTTON_Y, y) }
            5 -> { prefs.putFloat(KEY_SELL_CONFIRM_X, x); prefs.putFloat(KEY_SELL_CONFIRM_Y, y) }
        }
        prefs.apply()
    }
    
    // 获取配置坐标 - 做多
    fun getBuyConfig(context: Context): Map<String, Pair<Float, Float>> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "event" to Pair(prefs.getFloat(KEY_BUY_EVENT_X, 0f), prefs.getFloat(KEY_BUY_EVENT_Y, 0f)),
            "time" to Pair(prefs.getFloat(KEY_BUY_TIME_X, 0f), prefs.getFloat(KEY_BUY_TIME_Y, 0f)),
            "amount" to Pair(prefs.getFloat(KEY_BUY_AMOUNT_X, 0f), prefs.getFloat(KEY_BUY_AMOUNT_Y, 0f)),
            "button" to Pair(prefs.getFloat(KEY_BUY_BUTTON_X, 0f), prefs.getFloat(KEY_BUY_BUTTON_Y, 0f)),
            "confirm" to Pair(prefs.getFloat(KEY_BUY_CONFIRM_X, 0f), prefs.getFloat(KEY_BUY_CONFIRM_Y, 0f))
        )
    }
    
    // 获取配置坐标 - 做空
    fun getSellConfig(context: Context): Map<String, Pair<Float, Float>> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "event" to Pair(prefs.getFloat(KEY_SELL_EVENT_X, 0f), prefs.getFloat(KEY_SELL_EVENT_Y, 0f)),
            "time" to Pair(prefs.getFloat(KEY_SELL_TIME_X, 0f), prefs.getFloat(KEY_SELL_TIME_Y, 0f)),
            "amount" to Pair(prefs.getFloat(KEY_SELL_AMOUNT_X, 0f), prefs.getFloat(KEY_SELL_AMOUNT_Y, 0f)),
            "button" to Pair(prefs.getFloat(KEY_SELL_BUTTON_X, 0f), prefs.getFloat(KEY_SELL_BUTTON_Y, 0f)),
            "confirm" to Pair(prefs.getFloat(KEY_SELL_CONFIRM_X, 0f), prefs.getFloat(KEY_SELL_CONFIRM_Y, 0f))
        )
    }
    
    // 检查配置是否完成
    fun isBuyConfigComplete(context: Context): Boolean {
        val config = getBuyConfig(context)
        return config.values.all { it.first != 0f && it.second != 0f }
    }
    
    fun isSellConfigComplete(context: Context): Boolean {
        val config = getSellConfig(context)
        return config.values.all { it.first != 0f && it.second != 0f }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}