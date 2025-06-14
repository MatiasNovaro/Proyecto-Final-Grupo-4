package ar.ort.edu.proyecto_final_grupo_4.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = if (enabled) onClick else { {} },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFFE76F51) else Color(0xFFD1D5DB),
            disabledContainerColor = Color(0xFFD1D5DB)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFF9CA3AF),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonPreview() {
    LoginButton(
        text = "Enabled Button",
        onClick = { /* Action */ },
        enabled = true
    )
}
