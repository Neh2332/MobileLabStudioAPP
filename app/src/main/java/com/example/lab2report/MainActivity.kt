package com.example.lab2report

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2report.Task
import com.example.lab2report.TaskAdapter
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var taskList = mutableListOf<Task>()

    // UI references
    private lateinit var tvTitle: TextView
    private lateinit var tvSensorStats: TextView

    // Shake detection variables
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var acceleration = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Initialize Shake detection
        lastAcceleration = SensorManager.GRAVITY_EARTH
        currentAcceleration = SensorManager.GRAVITY_EARTH
        acceleration = 0.00f

        tvSensorStats = findViewById(R.id.tvSensorStats)
        tvTitle = findViewById(R.id.tvTitle)

        //style the title
        tvTitle.setTextColor(Color.BLUE)
        tvTitle.paint.style = Paint.Style.FILL_AND_STROKE
        tvTitle.paint.strokeWidth = 2f
        tvTitle.setShadowLayer(1.5f, -1f, 1f, Color.BLACK)

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

        // Register Sensors
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister Sensors to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_LIGHT -> {
                    val lux = it.values[0]
                    tvSensorStats.text = "Light Level: $lux lux"
                    if (lux < 10) {
                         tvSensorStats.append(" (Dark)")
                         tvSensorStats.setTextColor(Color.DKGRAY)
                    } else {
                         tvSensorStats.append(" (Bright)")
                         tvSensorStats.setTextColor(Color.BLACK)
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    lastAcceleration = currentAcceleration
                    currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val delta = currentAcceleration - lastAcceleration
                    acceleration = acceleration * 0.9f + delta

                    // Shake threshold
                    if (acceleration > 12) {
                        onShakeDetected()
                    }
                }
            }
        }
    }

    private fun onShakeDetected() {
        Toast.makeText(this, "Shake Detected!", Toast.LENGTH_SHORT).show()
        // Change title color randomly on shake
        val randomColor = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        tvTitle.setTextColor(randomColor)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
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
