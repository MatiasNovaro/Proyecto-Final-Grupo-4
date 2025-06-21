package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String? = null,
    showBackButton: Boolean = true,
    showLogo: Boolean = true,
    logoResourceId: Int? = null,
    backgroundColor: Color = Color(0xFFF8F1E9),
    actions: @Composable RowScope.() -> Unit = {}
) {
    // Si no se proporciona título, usa el título automático basado en la ruta
    val displayTitle = title ?: run {
        when (navController.currentBackStackEntry?.destination?.route) {
            "Home" -> "Inicio"
            "add_med" -> "Agregar Medicación"
            "LoginScreen" -> "Iniciar Sesión"
            "RegisterScreen" -> "Registrarse"
            "EditMedications" -> "Editar Medicaciones"
            "History" -> "Historial"
            "BiometricLogin" -> "Acceso Biométrico"
            "Reminder" -> "Recordatorios"
            else -> {
                // Para rutas dinámicas con parámetros
                val route = navController.currentBackStackEntry?.destination?.route
                when {
                    route?.startsWith("confirmMedication/") == true -> "Confirmar Medicación"
                    route?.startsWith("medication_confirmation/") == true -> "Confirmar Medicación"
                    else -> "MediTracker" // Nombre por defecto de tu app
                }
            }
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (showBackButton && navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver atrás"
                    )
                }
            } else if (showLogo && logoResourceId != null) {
                Icon(
                    painter = painterResource(id = logoResourceId),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor
        ),
        modifier = modifier
    )
}

@Composable
@Preview
fun PreviewTopBar(){
    MaterialTheme{
        // Para el preview, no podemos usar NavController real
        // CustomTopBar(navController = rememberNavController())
    }
}