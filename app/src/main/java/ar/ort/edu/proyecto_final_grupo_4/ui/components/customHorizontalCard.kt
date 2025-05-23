package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.ort.edu.proyecto_final_grupo_4.R

enum class MedicineStatus {
    WARNING,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineCard(
    time: String,
    name: String,
    dosage: String,
    status: MedicineStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.clock_icon),
                    contentDescription = "Hora del medicamento",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))


                Text(
                    text = time,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(16.dp))


                Column {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dosage,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }


            Icon(
                imageVector = when (status) {
                    MedicineStatus.WARNING -> Icons.Default.Warning
                    MedicineStatus.SUCCESS -> Icons.Default.CheckCircle
                },
                contentDescription = when (status) {
                    MedicineStatus.WARNING -> "Advertencia"
                    MedicineStatus.SUCCESS -> "Completado"
                },
                tint = when (status) {
                    MedicineStatus.WARNING -> Color(0xFFFF5722)
                    MedicineStatus.SUCCESS -> Color(0xFF4CAF50)
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicineCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicineCard(
                time = "10:30",
                name = "Paracetamol",
                dosage = "1 Gramo",
                status = MedicineStatus.WARNING
            )

            MedicineCard(
                time = "14:00",
                name = "Ibuprofeno",
                dosage = "400mg",
                status = MedicineStatus.SUCCESS
            )
        }
    }
}