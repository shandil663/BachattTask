# 🧠 BachattTask - AI Voice Task Manager

BachattTask is a smart task management Android app that lets users manage their tasks entirely through voice commands using the Gemini API. Create, update, and delete tasks just by talking!

---

## ✨ Features

- 🎤 **Voice-Controlled Commands** – Add, update, or delete tasks using natural voice instructions.
- ✅ **Smart Parsing** – AI interprets context-specific phrases for task management.
- 🗓️ **Due Date & Status Management** – Set deadlines and mark tasks as pending/completed.
- 📦 **Local Storage** – Tasks are stored locally using Room DB.
- 🔄 **Dynamic Task Filtering** – Filter tasks by status.
- 📱 **Modern UI** – Clean, Compose-based responsive interface.

---

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (Local Persistence)
- **AI Integration**: Gemini API (via Retrofit)
- **Voice Input**: Speech-to-Text (Android SpeechRecognizer)
- **State Management**: StateFlow

---

## ⚙️ Commands Supported

Gemini recognizes flexible phrasing for:
- **CREATE** – e.g. "Add a task to call mom tomorrow"
- **UPDATE** – e.g. "Change due date of Call Mom to 12-05-2025"
- **DELETE** – e.g. "Remove Call Mom"

---

## 🚀 How It Works

1. User speaks a command.
2. Speech is converted to text and sent to Gemini with a structured prompt.
3. Gemini returns a structured JSON indicating what to do.
4. ViewModel parses the JSON and updates the Room database accordingly.

---
