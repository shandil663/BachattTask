package com.example.bachatt.data.local
import com.example.bachatt.domain.model.Task
import com.example.bachatt.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return dao.getAllTasks().map { it.map { it.toDomain() } }
    }

    override fun getTasksByStatus(status: String): Flow<List<Task>> {
        return dao.getTasksByStatus(status).map { it.map { it.toDomain() } }
    }

    override suspend fun insertTask(task: Task) {
        dao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task.toEntity())
    }
}
