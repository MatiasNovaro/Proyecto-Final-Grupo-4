package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.navigation.Screens

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val currentRoute = navController.currentDestination?.route

        NavigationBarItem(
            selected = currentRoute == Screens.Home.screen,
            onClick = { navController.navigate(Screens.Home.screen) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Inicio") }
        )

        NavigationBarItem(
            selected = currentRoute == Screens.AddMedication.screen,
            onClick = { navController.navigate(Screens.AddMedication.screen) },
            icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
            label = { Text("Agregar") },
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Edit, contentDescription = "Editar") },
            label = { Text("Editar") },
            selected = currentRoute == Screens.EditMedications.screen,
            onClick = { navController.navigate(Screens.EditMedications.screen) }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
            label = { Text("Historial") },
            selected = currentRoute == Screens.History.screen,
            onClick = { navController.navigate(Screens.History.screen) }
        )
    }
}
