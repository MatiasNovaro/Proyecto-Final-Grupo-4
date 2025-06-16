package ar.ort.edu.proyecto_final_grupo_4

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import ar.ort.edu.proyecto_final_grupo_4.navigation.Navigation
import ar.ort.edu.proyecto_final_grupo_4.ui.components.BottomNavigationBar
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomTopBar
import androidx.fragment.app.FragmentActivity
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.ProyectoFinalGrupo4Theme
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel // Agregar este import
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoFinalGrupo4Theme {

                // Crear el AuthViewModel aquí
                val authViewModel: AuthViewModel = viewModel()
                val navController = rememberNavController()
                val currentTitle = remember { mutableStateOf("Home") }

                val scheduleId = intent?.getLongExtra("scheduleId", -1) ?: -1
                val fromAlarm = intent?.getBooleanExtra("fromAlarm", false) ?: false

                if (fromAlarm && scheduleId != -1L) {
                    navController.navigate("confirmMedication/$scheduleId")
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CustomTopBar(
                            title = currentTitle.value,
                            modifier = Modifier.height(30.dp)
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Navigation(
                            onDestinationChanged = { title -> currentTitle.value = title },
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}