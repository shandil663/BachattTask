package com.example.bachatt.data.local

import com.example.bachatt.domain.model.Task

fun TaskEntity.toDomain(): Task {
    return Task(id, title, dueDate, status)
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(id, title, dueDate, status)
}
