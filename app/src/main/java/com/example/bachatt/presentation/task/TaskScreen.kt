package com.example.bachatt.presentation.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bachatt.domain.model.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()

    if (isListening) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("🎙️ Listening...", fontWeight = FontWeight.Bold) },
            text = { Text("Speak your command to Bachatt.") }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Your Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip("All", uiState.filter, viewModel::setFilter)
            FilterChip("Pending", uiState.filter, viewModel::setFilter, Color(0xFFFFA726))
            FilterChip("Completed", uiState.filter, viewModel::setFilter, Color(0xFF66BB6A))
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(modifier = Modifier.height(16.dp))}
            items(uiState.tasks) { task ->
                TaskItem(
                    task = task,
                    onDelete = { viewModel.deleteTask(task) }
                ) {
                    val updated = task.copy(
                        status = if (task.status == "Pending") "Completed" else "Pending"
                    )
                    viewModel.updateTask(updated)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp))}
        }
    }
}

@Composable
fun FilterChip(label: String, selected: String, onClick: (String) -> Unit, color: Color = Color.Gray) {
    val isSelected = label == selected
    val containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) color else color.copy(alpha = 0.7f)
    Button(
        onClick = { onClick(label) },
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text = label, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TaskItem(task: Task, onDelete: () -> Unit, onToggleStatus: () -> Unit) {
    val date = remember(task.dueDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.dueDate))
    }
    val statusColor = when (task.status) {
        "Completed" -> Color(0xFF66BB6A)
        "Pending" -> Color(0xFFFFA726)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Due: $date",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "Status: ${task.status}",
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onToggleStatus) {
                    Text("Toggle Status")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
