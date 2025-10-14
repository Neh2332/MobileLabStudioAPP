package com.example.lab2report

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //style the title
        val titleText = findViewById<TextView>(R.id.tvTitle)
        titleText.setTextColor(Color.BLUE)
        titleText.paint.style = Paint.Style.FILL_AND_STROKE
        titleText.paint.strokeWidth = 2f
        titleText.setShadowLayer(1.5f, -1f, 1f, Color.BLACK)

        //style the Add Task button
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#2196F3"))
            setStroke(4, Color.BLACK)
        }
        btnAddTask.background = circle
        btnAddTask.setTextColor(Color.WHITE)

        btnAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivityForResult(intent, 1) // <--- changed to get result
        }

        //setup RecyclerView
        dbHelper = TaskDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(taskList)
        recyclerView.adapter = adapter

        //setup search/filter using proper TextWatcher
        val searchInput = findViewById<EditText>(R.id.etSearch)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtered = taskList.filter { it.title.contains(s.toString(), true) }
                adapter.filterList(filtered)
            }
        })

        loadTasks() //initial load
    }

    override fun onResume() {
        super.onResume()
        loadTasks() //refresh tasks whenever returning from AddTaskActivity
    }

    //method added for dynamic updates
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadTasks() //refresh tasks immediately after adding a new one
        }
    }

    private fun loadTasks() {
        taskList.clear()
        taskList.addAll(dbHelper.getAllTasks())
        adapter.notifyDataSetChanged()
    }
}
