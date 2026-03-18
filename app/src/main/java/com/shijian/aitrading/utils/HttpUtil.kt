// HttpUtil.kt - HTTP网络请求
package com.shijian.aitrading.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object HttpUtil {
    
    suspend fun login(username: String, password: String, deviceId: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val url = URL("${Config.API_BASE}/login")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                
                val json = """{"username":"$username","password":"$password","deviceId":"$deviceId"}"""
                conn.outputStream.write(json.toByteArray())
                
                if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    
    suspend fun register(username: String, password: String, deviceId: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val url = URL("${Config.API_BASE}/register")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                
                val json = """{"username":"$username","password":"$password","deviceId":"$deviceId"}"""
                conn.outputStream.write(json.toByteArray())
                
                if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    
    suspend fun getUserInfo(token: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val url = URL("${Config.API_BASE}/user")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $token")
                
                if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    
    suspend fun getSignals(token: String): String? = 
        withContext(Dispatchers.IO) {
            try {
                val url = URL("${Config.API_BASE}/signals")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $token")
                
                if (conn.responseCode == 200) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}