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
fun CambiarNombreScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var userName by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val currentUser = authState.user
    val isFormValid = userName.isNotBlank()

    LaunchedEffect(currentUser) {
        currentUser?.displayName?.let { displayName ->
            userName = displayName
        }
    }

    LaunchedEffect(authState.isUsernameUpdateSuccess) {
        if (authState.isUsernameUpdateSuccess) {
            showSuccessDialog = true
            authViewModel.clearUsernameUpdateSuccess()
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
            text = "Cambiar Nombre",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        LoginInput(
            value = userName,
            onValueChange = { userName = it },
            placeholder = "Nombre de usuario",
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth()
        )

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
            text = if (authState.isLoading) "Actualizando..." else "Cambiar Nombre",
            onClick = {
                if (userName.isNotBlank()) {
                    authViewModel.updateUsername(userName)
                }
            },
            enabled = isFormValid && !authState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    text = "¡Nombre actualizado!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tu nombre de usuario ha sido cambiado exitosamente.")
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

    LaunchedEffect(userName) {
        if (authState.error != null) {
            authViewModel.clearError()
        }
    }
}