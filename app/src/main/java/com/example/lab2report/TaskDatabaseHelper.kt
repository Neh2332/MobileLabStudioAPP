package com.example.lab2report

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabaseHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            private const val DATABASE_NAME = "tasks.db"
            private const val DATABASE_VERSION = 3
            private const val TABLE_NAME = "tasks"
            private const val COLUMN_ID = "id"
            private const val COLUMN_TITLE = "title"
            private const val COLUMN_COLOR = "color"
            private const val COLUMN_IMAGE_URI = "image_uri"
            private const val COLUMN_DEADLINE = "deadline"
            private const val COLUMN_DESCRIPTION = "description"
        }

    override fun onCreate(db: SQLiteDatabase?){
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_COLOR TEXT," +
                "$COLUMN_IMAGE_URI TEXT," +
                "$COLUMN_DEADLINE TEXT," +
                "$COLUMN_DESCRIPTION TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion:Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IMAGE_URI TEXT")
        }
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_DEADLINE TEXT")
            db?.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_DESCRIPTION TEXT")
        }
    }

    fun addTask(title:String, color:String, imageUri: String?, deadline: String?, description: String?): Boolean {
        val db= this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_COLOR, color)
            put(COLUMN_IMAGE_URI, imageUri)
            put(COLUMN_DEADLINE, deadline)
            put(COLUMN_DESCRIPTION, description)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }
    
    fun updateTask(task: Task): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_COLOR, task.color)
            put(COLUMN_IMAGE_URI, task.imageUri)
            put(COLUMN_DEADLINE, task.deadline)
            put(COLUMN_DESCRIPTION, task.description)
        }
        val result = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
        db.close()
        return result > 0
    }
    
    fun deleteTask(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getTaskById(id: Int): Task? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_COLOR, COLUMN_IMAGE_URI, COLUMN_DEADLINE, COLUMN_DESCRIPTION),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var task: Task? = null
        cursor.use {
            if (it.moveToFirst()) {
                val taskId = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val color = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR))
                val imageUri = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
                val deadline = it.getString(it.getColumnIndexOrThrow(COLUMN_DEADLINE))
                val description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                task = Task(taskId, title, color, imageUri, deadline, description)
            }
        }
        return task
    }


    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_COLOR, COLUMN_IMAGE_URI, COLUMN_DEADLINE, COLUMN_DESCRIPTION),
            null, null, null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val color = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR))
                val imageUri = it.getString(it.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
                val deadline = it.getString(it.getColumnIndexOrThrow(COLUMN_DEADLINE))
                val description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                tasks.add(Task(id, title, color, imageUri, deadline, description))
            }
        }
        return tasks
    }

}
