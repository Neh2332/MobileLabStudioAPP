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
            private const val DATABASE_VERSION = 1
            private const val TABLE_NAME = "tasks"
            private const val COLUMN_ID = "id"
            private const val COLUMN_TITLE = "title"
            private const val COLUMN_COLOR = "color"
        }

    override fun onCreate(db: SQLiteDatabase?){
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_COLOR TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion:Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTask(title:String, color:String): Boolean {
        val db= this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_COLOR, color)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }


    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_COLOR),
            null, null, null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val color = it.getString(it.getColumnIndexOrThrow(COLUMN_COLOR))
                tasks.add(Task(id, title, color))
            }
        }
        return tasks
    }

}
