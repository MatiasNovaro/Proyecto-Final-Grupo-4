package ar.ort.edu.proyecto_final_grupo_4.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Home : BottomNavItem("home", Icons.Default.Home, "Inicio")
    data object AddMedication : BottomNavItem("add_med", Icons.Default.AddCircle, "Agregar")
}
