package com.example.bachatt.domain.model

data class Task(
    val id: Int = 0,
    val title: String,
    val dueDate: Long,
    val status: String
)
