package com.example.bachatt.presentation.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bachatt.domain.model.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val state by viewModel.uiState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()

    if (isListening) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Listening...") },
            text = { Text("Speak your command to Bachatt.") }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.padding(top = 40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.setFilter("All") }) { Text("All") }
            Button(onClick = { viewModel.setFilter("Pending") }) { Text("Pending") }
            Button(onClick = { viewModel.setFilter("Completed") }) { Text("Completed") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(state.tasks) { task ->
                TaskItem(task = task, onDelete = { viewModel.deleteTask(task) }) {
                    val updated = task.copy(status = if (task.status == "Pending") "Completed" else "Pending")
                    viewModel.updateTask(updated)
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onDelete: () -> Unit, onToggleStatus: () -> Unit) {
    val date = remember(task.dueDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.dueDate))
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Due: $date")
            Text(text = "Status: ${task.status}")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = onToggleStatus) {
                    Text("Toggle Status")
                }
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            }
        }
    }
}
