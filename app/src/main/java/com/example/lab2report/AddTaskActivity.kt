package com.example.lab2report

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val etDeadline = findViewById<EditText>(R.id.etDeadline)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnDone = findViewById<Button>(R.id.btnDone)

        // Style circular buttons
        styleCircularButton(btnBack, Color.parseColor("#2196F3"), Color.BLACK)
        styleCircularButton(btnDone, Color.parseColor("#2196F3"), Color.BLACK)

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

        // Done button
        btnDone.setOnClickListener {
            finish()
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
