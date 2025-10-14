package com.example.lab2report

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var titleEditText: EditText
    private lateinit var colorGroup: RadioGroup
    private lateinit var doneButton: Button
    private var selectedColor: String = "#FFFFFF" // default color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        dbHelper = TaskDatabaseHelper(this)

        titleEditText = findViewById(R.id.etTaskTitle)
        colorGroup = findViewById(R.id.colorGroup)
        doneButton = findViewById(R.id.btnDone)
        val etDeadline = findViewById<EditText>(R.id.etDeadline)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Style circular buttons
        styleCircularButton(btnBack, Color.parseColor("#2196F3"), Color.BLACK)
        styleCircularButton(doneButton, Color.parseColor("#2196F3"), Color.BLACK)

        // Date picker for deadline
        etDeadline.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, y, m, d ->
                etDeadline.setText("$d/${m + 1}/$y")
            }, year, month, day)
            dpd.show()
        }

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Color selection
        colorGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedColor = when (checkedId) {
                R.id.colorRed -> "#FF0000"
                R.id.colorGreen -> "#00FF00"
                R.id.colorBlue -> "#0000FF"
                else -> "#FFFFFF"
            }
        }

        // Done button - validate and save
        doneButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addTask(title, selectedColor)
                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun styleCircularButton(button: Button, fillColor: Int, strokeColor: Int) {
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor) // Fill
            setStroke(4, strokeColor) // Outline
        }
        button.background = circle
        button.setTextColor(Color.WHITE)
    }
}
