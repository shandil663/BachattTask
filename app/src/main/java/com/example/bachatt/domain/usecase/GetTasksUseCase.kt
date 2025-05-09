package com.example.bachatt.domain.usecase
import com.example.bachatt.domain.model.Task
import com.example.bachatt.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getAllTasks()
}
