// SignalAdapter.kt - 信号列表适配器
package com.shijian.aitrading.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shijian.aitrading.R
import com.shijian.aitrading.ui.MainActivity

class SignalAdapter(private val signals: List<MainActivity.SignalRecord>) : 
    RecyclerView.Adapter<SignalAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvType: TextView = view.findViewById(R.id.tv_type)
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_signal, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val signal = signals[position]
        holder.tvTime.text = signal.time
        holder.tvType.text = signal.type
        holder.tvMessage.text = signal.message
        
        // 根据类型设置颜色
        val color = if (signal.type == "多") {
            holder.itemView.context.getColor(android.R.color.holo_green_dark)
        } else {
            holder.itemView.context.getColor(android.R.color.holo_red_dark)
        }
        holder.tvType.setTextColor(color)
    }
    
    override fun getItemCount() = signals.size
}