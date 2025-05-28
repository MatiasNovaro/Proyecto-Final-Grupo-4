package ar.ort.edu.proyecto_final_grupo_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.ort.edu.proyecto_final_grupo_4.ui.components.BottomNavigationBar
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomTopBar
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.Screens
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.addMedication.AddMedicationScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard.HomeScreen
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.ProyectoFinalGrupo4Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoFinalGrupo4Theme {

                val navController = rememberNavController()
                val currentTitle = remember { mutableStateOf("Home") }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CustomTopBar(
                            title = currentTitle.value,
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) {
                    innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        //verticalArrangement = Arrangement.Center
                    ) {
                        Navigation(
                            onDestinationChanged = { title -> currentTitle.value = title },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProyectoFinalGrupo4Theme {
        Greeting("Android")
    }
}