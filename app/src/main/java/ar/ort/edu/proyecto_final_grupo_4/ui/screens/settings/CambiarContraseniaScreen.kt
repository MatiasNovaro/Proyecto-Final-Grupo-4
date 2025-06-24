package ar.ort.edu.proyecto_final_grupo_4.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.ui.components.LoginButton
import ar.ort.edu.proyecto_final_grupo_4.ui.components.LoginInput
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.AuthViewModel

@Composable
fun CambiarContraseniaScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var password by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val isFormValid = password.isNotBlank() &&
            oldPassword.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            password == confirmPassword &&
            password.length >= 6

    // Observar el éxito de la actualización de contraseña
    LaunchedEffect(authState.isPasswordUpdateSuccess) {
        if (authState.isPasswordUpdateSuccess) {
            showSuccessDialog = true
            authViewModel.clearPasswordUpdateSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F1E9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Cambiar",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Contraseña",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        LoginInput(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            placeholder = "Contraseña actual",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginInput(
            value = password,
            onValueChange = { password = it },
            placeholder = "Nueva contraseña",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginInput(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "Confirmar nueva contraseña",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        // Mostrar validaciones
        if (password.isNotEmpty() && password.length < 6) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "La contraseña debe tener al menos 6 caracteres",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Las contraseñas no coinciden",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mostrar error si existe
        authState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        LoginButton(
            text = if (authState.isLoading) "Actualizando..." else "Cambiar Contraseña",
            onClick = {
                if (isFormValid) {
                    authViewModel.updatePassword(oldPassword, password)
                }
            },
            enabled = isFormValid && !authState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    text = "¡Contraseña actualizada!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tu contraseña ha sido cambiada exitosamente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Limpiar error cuando el usuario empiece a escribir
    LaunchedEffect(oldPassword, password, confirmPassword) {
        if (authState.error != null) {
            authViewModel.clearError()
        }
    }
}