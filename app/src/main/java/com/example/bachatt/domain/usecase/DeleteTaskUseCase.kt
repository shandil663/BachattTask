package com.example.bachatt.domain.usecase

import com.example.bachatt.domain.model.Task
import com.example.bachatt.domain.repository.TaskRepository

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
}
