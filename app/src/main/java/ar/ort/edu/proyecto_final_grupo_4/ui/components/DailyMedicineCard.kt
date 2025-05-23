package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CustomHorizontalCard(
    val time: String,
    val name: String,
    val dosage: String,
    val status: MedicineStatus
)

@Composable
fun DailyMedicineCard(
    medicines: List<CustomHorizontalCard> = listOf(
        CustomHorizontalCard("10:30", "Paracetamol", "1 Gramo", MedicineStatus.WARNING),
        CustomHorizontalCard("10:30", "Paracetamol", "1 Gramo", MedicineStatus.SUCCESS)
    ),
    dayTitle: String = "Hoy",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = dayTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                thickness = 1.dp,
                color = Color.Black
            )


            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                medicines.forEach { medicine ->
                    MedicineCard(
                        time = medicine.time,
                        name = medicine.name,
                        dosage = medicine.dosage,
                        status = medicine.status
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailyMedicineCardPreview() {
    MaterialTheme {
        DailyMedicineCard(
            modifier = Modifier.padding(16.dp)
        )
    }
}