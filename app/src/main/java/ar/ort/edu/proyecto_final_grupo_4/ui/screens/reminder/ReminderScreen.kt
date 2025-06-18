package ar.ort.edu.proyecto_final_grupo_4.ui.screens.reminder

import androidx.hilt.navigation.compose.hiltViewModel
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.LightCream
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.PrimaryOrange
import ar.ort.edu.proyecto_final_grupo_4.ui.theme.GreenConfirm
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ar.ort.edu.proyecto_final_grupo_4.R
import ar.ort.edu.proyecto_final_grupo_4.domain.model.Schedule
import ar.ort.edu.proyecto_final_grupo_4.ui.components.CustomButton
import ar.ort.edu.proyecto_final_grupo_4.viewmodel.ReminderViewModel


@Composable
fun ReminderScreen(
    navController: NavController,
    schedule: Schedule,
    scheduleIds: List<Long>,
    fromNotification: Boolean
) {

    val reminderViewModel: ReminderViewModel = hiltViewModel()
    LaunchedEffect(schedule) {
        reminderViewModel.loadMedication(schedule)
    }
    val medicationState = reminderViewModel.medication.collectAsState()
    val medication = medicationState.value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color=PrimaryOrange) // Fondo de pantalla con el color primario
    ) {
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 120.dp) // Desplaza la columna aún más hacia abajo
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) // Esquinas superiores redondeadas
                .background(color=LightCream) // Color secundario
                .padding(24.dp)
                .align(Alignment.TopCenter), // Alineado en la parte superior con desplazamiento
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Más espacio entre elementos
        ) {
            // Contenedor para las frases
            Box(modifier = Modifier.fillMaxWidth(0.8f)) {
                // Texto arriba de la imagen
                Text(
                    text = stringResource(id = R.string.reminder_screen_phrase),
                    color = Color.White,
                    fontSize = 33.sp, // Tamaño de fuente más grande
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Imagen
            Image(
                painter = painterResource(id = R.drawable.pill_foreground),
                contentDescription = null,
                modifier = Modifier.size(150.dp) // Imagen más grande
            )

            // Contenedor para las frases
            Box(modifier = Modifier.fillMaxWidth(0.8f)) {
                // Texto abajo de la imagen
                @androidx.compose.runtime.Composable {
                    if (medication != null) {
                        Text(
                            text = medication.name,
                            color = Color.White,
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Cargando medicamento...",
                            color = Color.White,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Botón "Lo tomé"
                CustomButton(
                    buttonColor = GreenConfirm,
                    text = stringResource(id = R.string.reminder_screen_btn_already_took_it),
                    modifier = Modifier
                        .height(80.dp) // Botón más alto
                        .fillMaxWidth(0.9f), // Botón más ancho
                    onClick = { reminderViewModel.medicationTaken(schedule) }
                )

                // Botón "Recordar más tarde"
                @Composable
                fun ElevatedButtonExample(onClick: () -> Unit) {
                    ElevatedButton(onClick = { reminderViewModel.medicationNotTaken(schedule) }) {
                        Text("Recordar más tarde",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp),
                            color = Black) // Texto del botón)
                    }
                }
            }
        }
    }}

