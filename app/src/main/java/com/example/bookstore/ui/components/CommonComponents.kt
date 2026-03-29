package com.example.bookstore.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Pending" -> Color(0xFFFFA000)
        "Processing" -> Color(0xFF1976D2)
        "Shipped" -> Color(0xFF7B1FA2)
        "Delivered" -> Color(0xFF388E3C)
        "Cancelled" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
