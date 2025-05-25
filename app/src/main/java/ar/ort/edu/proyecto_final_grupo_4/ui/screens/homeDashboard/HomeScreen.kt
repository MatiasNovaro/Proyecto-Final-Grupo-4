package ar.ort.edu.proyecto_final_grupo_4.ui.screens.homeDashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomButton
import ar.ort.edu.proyecto_final_grupo_4.ui.components.DailyMedicineCard
import ar.ort.edu.proyecto_final_grupo_4.ui.screens.Screens
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.LightCream
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.UserViewModel

@SuppressLint("SuspiciousIndentation")
@Composable
fun HomeScreen(navController: NavController){
  val userViewModel: UserViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        userViewModel.ensureDefaultUser()
    }
  val user by userViewModel.user.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween // Para poner el botón abajo
    ) {
        Column {
            MedicationHeader()
            DailyMedicineCard()
        }

        CustomButton(
            text = "Confirmar Toma",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            buttonColor = PrimaryOrange,
            onClick = {navController.navigate(Screens.AddMedication.screen)}
        )
    }

}

@Composable
fun MedicationHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Próxima\nmedicación en:",
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "2h 30min",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HomePreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}
