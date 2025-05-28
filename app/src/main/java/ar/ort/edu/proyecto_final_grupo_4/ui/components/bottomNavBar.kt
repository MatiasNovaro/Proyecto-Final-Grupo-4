package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import ar.ort.edu.proyecto_final_grupo_4.BottomNavItem
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.Screens

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
            label = { Text("Agregar") }
        )
    }
}
