package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.ServiceRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ServiceViewModel
import com.example.viewmodel.ServiceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize SQLite Database via Room abstract builder
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "argenis_paredes_construction_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        // 2. Instantiate repository that manages work orders and custom requests
        val repository = ServiceRepository(database.serviceDao())

        // 3. Coordinate State Flow with ViewModel
        val viewModel = ViewModelProvider(
            this,
            ServiceViewModelFactory(repository)
        )[ServiceViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Padding is consumed inside MainScreen relative to tabs/bars
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// Composable function reserved to maintain standard platform unit/screenshot verification tests
@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
