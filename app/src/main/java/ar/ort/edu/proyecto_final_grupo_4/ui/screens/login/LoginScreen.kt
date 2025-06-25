package ar.ort.edu.proyecto_final_grupo_4.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.R
import ar.ort.edu.proyecto_final_grupo_4.ui.components.LoginButton
import ar.ort.edu.proyecto_final_grupo_4.ui.components.LoginInput
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val isFormValid = email.isNotBlank() && password.isNotBlank()

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess && authState.user != null) {
            authViewModel.clearSuccess()
            navController.navigate("Home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F1E9))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // Logo de la app
        Image(
            painter = painterResource(id = R.drawable.logo_app),
            contentDescription = "Logo de miMedicación",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )

        // Texto promocional
        val promotionalText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Gray)) {
                append("Con ")
            }
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = PrimaryOrange
            )) {
                append("miMedicación")
            }
            withStyle(style = SpanStyle(color = Color.Gray)) {
                append(" organizá tus tomas. Controlá tu medicación. Mejorá tu salud.")
            }
        }

        Text(
            text = promotionalText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B4E3D),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Text(
            text = "¡Bienvenido!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        LoginInput(
            value = email,
            onValueChange = { email = it },
            placeholder = "Email",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginInput(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        if (authState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = authState.error!!,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Spacer(modifier = Modifier.weight(1f))

        val annotatedText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Gray)) {
                append("¿No tienes una cuenta? ")
            }
            withStyle(style = SpanStyle(color = Color(0xFFE76F51), fontWeight = FontWeight.Bold)) {
                append("Registrarse")
            }
        }

        Text(
            text = annotatedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("RegisterScreen")}
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoginButton(
            text = if (authState.isLoading) "Cargando..." else "Empezar",
            onClick = {
                authViewModel.clearError()
                authViewModel.signIn(email, password)
            },
            enabled = isFormValid && !authState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}