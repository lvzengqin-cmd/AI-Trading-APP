// LogAdapter.kt - 日志列表适配器
package com.shijian.aitrading.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shijian.aitrading.R

class LogAdapter(private val logs: List<String>) : 
    RecyclerView.Adapter<LogAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLog: TextView = view.findViewById(R.id.tv_log)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvLog.text = logs[position]
    }
    
    override fun getItemCount() = logs.size
}