package com.example.lab2report

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2report.Task
import com.example.lab2report.R
import com.example.lab2report.EditTaskActivity

class TaskAdapter(private var tasks: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder for each task row
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val taskImageView: ImageView = itemView.findViewById(R.id.ivTaskImage)
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
        try {
            holder.itemView.setBackgroundColor(Color.parseColor(task.color))
        } catch (e: IllegalArgumentException) {
            holder.itemView.setBackgroundColor(Color.WHITE) // Default color
        }

        // Load image if URI is not null
        task.imageUri?.let {
            holder.taskImageView.visibility = View.VISIBLE
            try {
                holder.taskImageView.setImageURI(Uri.parse(it))
            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally set a placeholder or error image, or hide the view
                // holder.taskImageView.setImageResource(R.drawable.error_image) 
            }
        } ?: run {
            holder.taskImageView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditTaskActivity::class.java)
            intent.putExtra("task_id", task.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tasks.size

    // For search/filter functionality
    fun filterList(filteredTasks: List<Task>) {
        tasks = filteredTasks
        notifyDataSetChanged()
    }
}