package ar.ort.edu.proyecto_final_grupo_4.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomHorizontalCard
import ar.ort.edu.proyecto_final_grupo_4.ui.components.DailyMedicineCard
import ar.ort.edu.proyecto_final_grupo_4.ui.components.MedicineStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    val medicineHistory = listOf(
        DayMedicineData(
            dayTitle = "Hoy",
            medicines = listOf(
                CustomHorizontalCard("10:30", "Paracetamol", "1 Gramo", MedicineStatus.WARNING),
                CustomHorizontalCard("10:30", "Paracetamol", "1 Gramo", MedicineStatus.SUCCESS)
            )
        ),
        DayMedicineData(
            dayTitle = "Ayer",
            medicines = listOf(
                CustomHorizontalCard("10:30", "Paracetamol", "1 Gramo", MedicineStatus.WARNING),
                CustomHorizontalCard("10:30", "Paracetamol", "1 Gramo", MedicineStatus.SUCCESS)
            )
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F1E9))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Historial",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
//            items(medicineHistory) { dayData ->
//                DailyMedicineCard(
//                    medicines = dayData.medicines,
//                    dayTitle = dayData.dayTitle
//                )
          //  }
        }
    }
}


data class DayMedicineData(
    val dayTitle: String,
    val medicines: List<CustomHorizontalCard>
)

@Preview(showBackground = true)
@Composable
fun HistorialScreenPreview() {
    MaterialTheme {
        HistorialScreen()
    }
}