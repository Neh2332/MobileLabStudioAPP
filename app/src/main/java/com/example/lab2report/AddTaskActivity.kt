package com.example.lab2report

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AddTaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var titleEditText: EditText
    private lateinit var etDeadline: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var doneButton: Button
    private lateinit var addImageButton: Button
    private lateinit var taskImageView: ImageView

    private var selectedColor: String = "#FFFFFF" // default color
    private var imageUri: Uri? = null
    private var taskId: Int = -1

    private val CAMERA_PERMISSION_CODE = 100
    private val GALLERY_PERMISSION_CODE = 101
    private val IMAGE_PICK_GALLERY_CODE = 102
    private val IMAGE_CAPTURE_CODE = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        dbHelper = TaskDatabaseHelper(this)

        titleEditText = findViewById(R.id.etTaskTitle)
        etDeadline = findViewById(R.id.etDeadline)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        doneButton = findViewById(R.id.btnDone)
        addImageButton = findViewById(R.id.btnAddImage)
        taskImageView = findViewById(R.id.ivTaskImage)

        val btnBack = findViewById<Button>(R.id.btnBack)

        // Style circular buttons
        styleCircularButton(btnBack, Color.parseColor("#2196F3"), Color.BLACK)
        styleCircularButton(doneButton, Color.parseColor("#2196F3"), Color.BLACK)

        // Check if we are editing an existing task
        taskId = intent.getIntExtra("task_id", -1)
        if (taskId != -1) {
            val task = dbHelper.getTaskById(taskId)
            task?.let {
                titleEditText.setText(it.title)
                etDeadline.setText(it.deadline)
                etTaskDescription.setText(it.description)
                selectedColor = it.color
                it.imageUri?.let {
                    imageUri = Uri.parse(it)
                    taskImageView.setImageURI(imageUri)
                    taskImageView.visibility = View.VISIBLE
                }
            }
        }

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
        findViewById<View>(R.id.viewColorRed).setOnClickListener { selectedColor = it.tag.toString() }
        findViewById<View>(R.id.viewColorGreen).setOnClickListener { selectedColor = it.tag.toString() }
        findViewById<View>(R.id.viewColorBlue).setOnClickListener { selectedColor = it.tag.toString() }
        findViewById<View>(R.id.viewColorYellow).setOnClickListener { selectedColor = it.tag.toString() }

        // Add image button
        addImageButton.setOnClickListener {
            showImagePickerDialog()
        }

        // Done button - validate and save
        doneButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val deadline = etDeadline.text.toString().trim()
            val description = etTaskDescription.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            } else {
                if (taskId == -1) {
                    dbHelper.addTask(title, selectedColor, imageUri?.toString(), deadline, description)
                    Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
                } else {
                    val updatedTask = Task(taskId, title, selectedColor, imageUri?.toString(), deadline, description)
                    dbHelper.updateTask(updatedTask)
                    Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show()
                }
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Image")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> checkGalleryPermission()
                    2 -> dialog.dismiss()
                }
            }.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    private fun checkGalleryPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), GALLERY_PERMISSION_CODE)
            } else {
                openGallery()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), GALLERY_PERMISSION_CODE)
            } else {
                openGallery()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, IMAGE_PICK_GALLERY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_GALLERY_CODE -> {
                    imageUri = data?.data
                    taskImageView.setImageURI(imageUri)
                    taskImageView.visibility = View.VISIBLE
                }
                IMAGE_CAPTURE_CODE -> {
                    val photo = data?.extras?.get("data") as? android.graphics.Bitmap
                    photo?.let {
                        val contentResolver = contentResolver
                        val contentValues = android.content.ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "task_image_${System.currentTimeMillis()}.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        }
                        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        imageUri?.let {
                            this.imageUri = it
                            taskImageView.setImageURI(this.imageUri)
                            taskImageView.visibility = View.VISIBLE
                            try {
                                val outputStream = contentResolver.openOutputStream(it)
                                outputStream?.let {
                                    photo.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it)
                                    it.close()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
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
