package com.example.bachatt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.bachatt.data.local.TaskDatabase
import com.example.bachatt.data.local.TaskRepositoryImpl
import com.example.bachatt.domain.usecase.*
import com.example.bachatt.presentation.task.TaskScreen
import com.example.bachatt.presentation.task.TaskViewModel
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private var isCommandMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleTheme()
        val db = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepositoryImpl(db.taskDao())
        val viewModel = TaskViewModel(
            GetTasksUseCase(repository),
            AddTaskUseCase(repository),
            UpdateTaskUseCase(repository),
            DeleteTaskUseCase(repository)
        )

        setContent {
            val context = LocalContext.current
            var showDialog by remember { mutableStateOf(false) }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    startListening(viewModel) { showDialog = it }
                } else {
                    Toast.makeText(context, "Mic permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    startListening(viewModel) { showDialog = it }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("🎙️ Listening...",fontWeight = FontWeight.Bold) },
                    text = { Text("Speak your command to Bachatt.") },
                    confirmButton = {},
                    dismissButton = {}
                )
            }

            TaskScreen(viewModel)
        }
    }

    private fun startListening(viewModel: TaskViewModel, toggleDialog: (Boolean) -> Unit) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        }

        var hasReceivedValidCommand = false

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""

                if (isCommandMode) {
                    if (text.isNotBlank()) {
                        hasReceivedValidCommand = true
                        toggleDialog(false)
                        isCommandMode = false
                        viewModel.onVoiceCommandReceived(text)
                        Handler(Looper.getMainLooper()).postDelayed({
                            speechRecognizer.startListening(intent)
                        }, 1000)
                    } else {
                        speechRecognizer.startListening(intent)
                    }
                } else {
                    if (text.contains("hi bachat") || text.contains("hi bachatt")) {
                        toggleDialog(true)
                        isCommandMode = true
                        hasReceivedValidCommand = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            speechRecognizer.startListening(intent)
                        }, 1000)
                    } else {
                        speechRecognizer.startListening(intent)
                    }
                }
            }

            override fun onEndOfSpeech() {
                if (!hasReceivedValidCommand) {
                    speechRecognizer.startListening(intent)
                }
            }

            override fun onError(error: Int) {
                if (isCommandMode && !hasReceivedValidCommand) {
                    speechRecognizer.startListening(intent)
                } else {
                    toggleDialog(false)
                    isCommandMode = false

                    Handler(Looper.getMainLooper()).postDelayed({
                        speechRecognizer.startListening(intent)
                    }, 1000)
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }


    protected fun handleTheme() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars =
            true
    }
}
