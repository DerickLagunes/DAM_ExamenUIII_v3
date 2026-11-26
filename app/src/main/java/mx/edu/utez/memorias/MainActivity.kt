package mx.edu.utez.memorias

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import mx.edu.utez.memorias.data.local.UserSession
import mx.edu.utez.memorias.navigation.AppNavGraph
import mx.edu.utez.memorias.navigation.AppRoutes
import mx.edu.utez.memorias.ui.screens.LoginScreen
import mx.edu.utez.memorias.ui.screens.RegisterScreen
import mx.edu.utez.memorias.ui.theme.MemoriasTheme
import mx.edu.utez.memorias.ui.viewmodels.AuthViewModel
import mx.edu.utez.memorias.workers.LikeNotificationWorker

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val appContainer = (application as MemoriasApplication).appContainer

        setContent {
            MaterialTheme {
                AppNavGraph(appContainer = appContainer)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WorkManager.getInstance(applicationContext).enqueue(
            OneTimeWorkRequestBuilder<LikeNotificationWorker>().build()
        )
    }
}

//Etapa3
