package com.example.lab2report

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private var tasks: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder for each task row
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTaskTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set task title
        holder.titleTextView.text = task.title

        // Set background color based on task.color
        holder.titleTextView.setBackgroundColor(Color.parseColor(task.color))

        // Dynamically adjust height based on text length
        holder.titleTextView.height = 100 + task.title.length * 10
    }

    override fun getItemCount(): Int = tasks.size

    // For search/filter functionality
    fun filterList(filteredTasks: List<Task>) {
        tasks = filteredTasks
        notifyDataSetChanged()
    }
}
