package com.example.bachatt.presentation.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bachatt.data.remote.*
import com.example.bachatt.domain.model.Task
import com.example.bachatt.domain.usecase.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filter: String = "All"
)

class TaskViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {
    private val gson = Gson()
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    fun setListening(value: Boolean) {
        _isListening.value = value
    }

    init {
        viewModelScope.launch {
            getTasksUseCase().collect { tasks ->
                val filtered = when (_uiState.value.filter) {
                    "Pending" -> tasks.filter { it.status == "Pending" }
                    "Completed" -> tasks.filter { it.status == "Completed" }
                    else -> tasks
                }
                _uiState.update { it.copy(tasks = filtered) }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun addTask(task: Task) {
        viewModelScope.launch { addTaskUseCase(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { updateTaskUseCase(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { deleteTaskUseCase(task) }
    }

    fun onVoiceCommandReceived(command: String) {
        viewModelScope.launch {
            try {
                val response = GeminiService.api.generateContent(
                    apiKey = "AIzaSyC-UarC5-DSa_3hlEhz6l1mjNJDp4SnaMA",
                    body = GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(GeminiPart(
                                    text = command + "\n\n\n" + """You are a task management assistant. Your job is to extract structured task data from the given input. Respond only with a JSON object, nothing else.
There are 3 types of operations:
1. "CREATE": When the user wants to add a new task.
2. "UPDATE": When the user wants to change the title, due date, or status of an existing task.
3. "DELETE": When the user wants to remove a task.

Return only these fields based on the operation:
- "operation": One of "CREATE", "UPDATE", or "DELETE"
- "title": The task's title (refined to max 2 words)
- "dueAt": Only if specified, date in DD-MM-YYYY format (strictly)
- "status": One of "PENDING" or "COMPLETED" (only when relevant)

Examples:
CREATE: { "operation": "CREATE", "title": "Pay Bills", "dueAt": "10-05-2025", "status": "PENDING" }
UPDATE: { "operation": "UPDATE", "title": "Pay Bills", "dueAt": "15-05-2025", "status": "COMPLETED" }
DELETE: { "operation": "DELETE", "title": "Pay Bills" }

Do not return any explanation or formatting. Your reply must be a valid JSON object. Use 2025 as the year for any due date."""
                                ))
                            )
                        )
                    )
                )

                val reply = cleanJson(response.candidates.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text ?: return@launch)

                Log.d("Gemini", "AI Cleaned JSON: $reply")
                parseGeminiReply(reply)
            } catch (e: Exception) {
                Log.e("VoiceCommandError", "Error while calling Gemini API", e)
                e.printStackTrace()
                val fallback = Task(title = "Error", dueDate = System.currentTimeMillis(), status = "Pending")
                addTask(fallback)
            }
        }
    }

    private fun parseGeminiReply(reply: String) {
        try {
            val taskMap = gson.fromJson(reply, Map::class.java) as Map<String, String>
            val operation = taskMap["operation"]?.uppercase()?.trim()
            val title = taskMap["title"]?.trim().orEmpty()
            val dueAtStr = taskMap["dueAt"]?.trim().orEmpty()
            val status = taskMap["status"]?.uppercase() ?: "PENDING"
            if (title.isBlank()) return

            val dueDate = if (dueAtStr.matches(Regex("\\d{2}-\\d{2}-\\d{4}")))
                SimpleDateFormat("dd-MM-yyyy").parse(dueAtStr)?.time
            else null

            when (operation) {
                "CREATE" -> {
                    val task = Task(title = title, dueDate = dueDate ?: System.currentTimeMillis(), status = "Pending")
                    addTask(task)
                }
                "UPDATE" -> {
                    val existing = _uiState.value.tasks.find { it.title.equals(title, ignoreCase = true) }
                    existing?.let {
                        val updated = it.copy(
                            title = title,
                            dueDate = dueDate ?: it.dueDate,
                            status = if (status == "COMPLETED") "Completed" else "Pending"
                        )
                        updateTask(updated)
                    }
                }
                "DELETE" -> {
                    val task = _uiState.value.tasks.find { it.title.equals(title, ignoreCase = true) }
                    task?.let { deleteTask(it) }
                }


            }

        } catch (e: Exception) {
            Log.e("ParseError", "Failed to parse Gemini JSON", e)
        }
    }

    private fun cleanJson(response: String): String {
        return response.trim()
            .removeSurrounding("```json", "```")
            .removeSurrounding("```")
    }
}
