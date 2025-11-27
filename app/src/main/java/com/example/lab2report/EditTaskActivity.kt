package com.example.lab2report

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class EditTaskActivity : AppCompatActivity() {

    private lateinit var dbHelper: TaskDatabaseHelper
    private var taskId: Int = -1
    private var selectedColor: String = "#FFFFFF"
    private var selectedImageUri: Uri? = null

    private lateinit var etTaskTitle: EditText
    private lateinit var etDeadline: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var ivTaskImage: ImageView
    private lateinit var btnAddImage: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button

    private val CAMERA_PERMISSION_CODE = 100
    private val GALLERY_PERMISSION_CODE = 101
    private val IMAGE_PICK_GALLERY_CODE = 102
    private val IMAGE_CAPTURE_CODE = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        dbHelper = TaskDatabaseHelper(this)

        etTaskTitle = findViewById(R.id.etTaskTitle)
        etDeadline = findViewById(R.id.etDeadline)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        ivTaskImage = findViewById(R.id.ivTaskImage)
        btnAddImage = findViewById(R.id.btnAddImage)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBack)

        taskId = intent.getIntExtra("task_id", -1)

        if (taskId != -1) {
            loadTaskData(taskId)
        }

        etDeadline.setOnClickListener { showDatePickerDialog() }
        btnAddImage.setOnClickListener { showImagePickerDialog() }
        btnUpdate.setOnClickListener { updateTask() }
        btnDelete.setOnClickListener { deleteTask() }
        btnBack.setOnClickListener { finish() }

        setupColorPickers()
    }

    private fun loadTaskData(id: Int) {
        val task = dbHelper.getTaskById(id)
        task?.let {
            etTaskTitle.setText(it.title)
            it.deadline?.let { etDeadline.setText(it) }
            it.description?.let { etTaskDescription.setText(it) }
            selectedColor = it.color
            applyColorToViews(selectedColor)
            it.imageUri?.let { uriString ->
                selectedImageUri = Uri.parse(uriString)
                try {
                    ivTaskImage.setImageURI(selectedImageUri)
                    ivTaskImage.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupColorPickers() {
        val colors = listOf("#F44336", "#4CAF50", "#2196F3", "#FFEB3B")
        val colorViews = listOf(R.id.viewColorRed, R.id.viewColorGreen, R.id.viewColorBlue, R.id.viewColorYellow)

        for (i in colors.indices) {
            val view = findViewById<View>(colorViews[i])
            view.setOnClickListener { 
                selectedColor = colors[i]
                applyColorToViews(selectedColor)
            }
            // Set initial background color
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Color.parseColor(colors[i]))
            view.background = drawable
        }
    }

    private fun applyColorToViews(color: String) {
        // Apply color to a representative view, e.g., a border around the selected color view
        // For now, just update the selectedColor variable
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            etDeadline.setText(date)
        }, year, month, day)
        dpd.show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
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
                    selectedImageUri = data?.data
                    selectedImageUri?.let { uri ->
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                        ivTaskImage.setImageURI(uri)
                        ivTaskImage.visibility = View.VISIBLE
                    }
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
                            selectedImageUri = it
                            ivTaskImage.setImageURI(selectedImageUri)
                            ivTaskImage.visibility = View.VISIBLE
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

    private fun updateTask() {
        val title = etTaskTitle.text.toString()
        val deadline = etDeadline.text.toString()
        val description = etTaskDescription.text.toString()
        val imageUriString = selectedImageUri?.toString()

        if (title.isBlank()) {
            Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTask = Task(taskId, title, selectedColor, imageUriString, deadline, description)

        dbHelper.updateTask(updatedTask)
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun deleteTask() {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ ->
                dbHelper.deleteTask(taskId)
                setResult(Activity.RESULT_OK)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
