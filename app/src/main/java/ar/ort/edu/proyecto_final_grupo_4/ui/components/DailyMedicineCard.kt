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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleWithDetails
import java.time.format.DateTimeFormatter

data class CustomHorizontalCard(
    val time: String,
    val name: String,
    val dosage: String,
    val status: MedicineStatus
)

@Composable
fun DailyMedicineCard(
    medicines: List<ScheduleWithDetails>,
    dayTitle: String = "Hoy",
    modifier: Modifier,

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

            // Ajuste para permitir scroll en una altura máxima para 3 elementos
            val maxVisibleItems = 3
            val itemHeight = 85.dp // Ajustá esto según tu diseño real
            val maxHeight = itemHeight * maxVisibleItems

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight) // Máximo espacio visible
            ) {
                items(medicines) { medicine ->
                    println(medicine.isCompletedToday)
                    var status  = MedicineStatus.SUCCESS
                    if(!medicine.isCompletedToday) status = MedicineStatus.WARNING
                    val timeFormatted = medicine.nextDose?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm"))
                    if (timeFormatted != null) {
                        MedicineCard(
                            time = timeFormatted,
                            name = medicine.medication.name,
                            dosage = "${medicine.medication.dosage} ${medicine.dosageUnit.name}",
                            status = status
                        )
                    }
                }
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun DailyMedicineCardPreview() {
//    MaterialTheme {
//        DailyMedicineCard(
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}