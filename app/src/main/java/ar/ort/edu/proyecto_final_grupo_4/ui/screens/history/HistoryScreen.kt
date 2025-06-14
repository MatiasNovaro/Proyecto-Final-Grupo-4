package ar.ort.edu.proyecto_final_grupo_4.ui.screens.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.LightCream
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ScheduleViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()
    val historySchedules by scheduleViewModel.historySchedules.collectAsState()

    LaunchedEffect(Unit) {
        scheduleViewModel.loadHistorySchedules()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightCream
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightCream)
                .padding(horizontal = 16.dp)
                .padding(top = 80.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                val groupedSchedules = historySchedules.groupBy { it.dayLabel }

                items(groupedSchedules.keys.toList()) { dayLabel ->
                    val schedulesForDay = groupedSchedules[dayLabel] ?: emptyList()
                    HistoryDaySection(
                        dayLabel = dayLabel,
                        schedules = schedulesForDay
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun HistoryDaySection(
    dayLabel: String,
    schedules: List<ScheduleHistoryItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dayLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            schedules.forEach { schedule ->
                HistoryMedicationItem(schedule = schedule)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HistoryMedicationItem(schedule: ScheduleHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Hora",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = schedule.time,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.width(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = schedule.medicationName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = schedule.dosage,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Icon(
            imageVector = if (schedule.isTaken) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = if (schedule.isTaken) "Tomado" else "No tomado",
            tint = if (schedule.isTaken) Color.Green else PrimaryOrange,
            modifier = Modifier.size(24.dp)
        )
    }
}

data class ScheduleHistoryItem(
    val id: String,
    val medicationName: String,
    val dosage: String,
    val time: String,
    val isTaken: Boolean,
    val dayLabel: String // "Hoy", "Ayer", etc.
)

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    MaterialTheme {
        HistoryScreen(navController = rememberNavController())
    }
}