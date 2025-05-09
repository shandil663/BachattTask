package com.example.bachatt.domain.usecase

import com.example.bachatt.domain.model.Task
import com.example.bachatt.domain.repository.TaskRepository


class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task)
    }
}
