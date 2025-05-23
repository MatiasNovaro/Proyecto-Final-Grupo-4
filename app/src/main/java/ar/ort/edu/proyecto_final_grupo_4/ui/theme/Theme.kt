package ar.ort.edu.proyecto_final_grupo_4.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,             // Botones principales, AM seleccionado
    onPrimary = White,                     // Texto sobre el botón naranja
    secondary = PrimaryOrange,           // Uso secundario (puede ser igual al primary)
    onSecondary = White,
    background = LightCream,               // Fondo general
    onBackground = Color.Black,
    surface = LightCream,                  // Fondo de tarjetas (como Surface)
    onSurface = Color.Black,
    surfaceVariant = LightCream,           // Fondo del reloj / variantes suaves
    onSurfaceVariant = Color.Black,
    outline = PrimaryOrange
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,             // Botones principales, AM seleccionado
    onPrimary = White,                     // Texto sobre el botón naranja
    secondary = PrimaryOrange,           // Uso secundario (puede ser igual al primary)
    onSecondary = White,
    background = LightCream,               // Fondo general
    onBackground = Color.Black,
    surface = LightCream,                  // Fondo de tarjetas (como Surface)
    onSurface = Color.Black,
    surfaceVariant = LightCream,           // Fondo del reloj / variantes suaves
    onSurfaceVariant = Color.Black,
    outline = PrimaryOrange
)

@Composable
fun ProyectoFinalGrupo4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}