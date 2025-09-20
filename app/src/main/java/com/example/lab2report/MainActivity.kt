package com.example.lab2report

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Style the title
        val titleText = findViewById<TextView>(R.id.tvTitle)
        titleText.setTextColor(Color.BLUE)
        titleText.paint.style = Paint.Style.FILL_AND_STROKE
        titleText.paint.strokeWidth = 2f
        titleText.setShadowLayer(1.5f, -1f, 1f, Color.BLACK)

        // Style the big circular Add Task button
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#2196F3")) // Blue fill
            setStroke(4, Color.BLACK) // Black outline
        }
        btnAddTask.background = circle
        btnAddTask.setTextColor(Color.WHITE)

        btnAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }
}
