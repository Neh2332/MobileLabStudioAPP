package com.example.lab2report

data class Task (
    val id: Int,
    val title: String,
    val color: String,
    val imageUri: String?,
    val deadline: String?,
    val description: String?
)