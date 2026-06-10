package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.TaskRepository

class SereneTasksApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}
