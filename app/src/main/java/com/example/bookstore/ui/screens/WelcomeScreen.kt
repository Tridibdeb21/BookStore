package com.example.bookstore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookstore.viewmodel.AuthState
import com.example.bookstore.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onNavigateToMain()
        }
    }

    if (authState !is AuthState.Success) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D))
        ) {
            // Radial gold glow background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD4A853).copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(0.5f * 1080f, 0.3f * 1920f),
                            radius = 700f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Top brand strip
                Spacer(modifier = Modifier.height(64.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 28.dp)
                            .background(Color(0xFFD4A853), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "BOOKSTORE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        color = Color(0xFFD4A853)
                    )
                }

                // Hero text
                Spacer(modifier = Modifier.weight(0.3f))
                Text(
                    text = "Your\nLiterary\nUniverse.",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 64.sp,
                    letterSpacing = (-2).sp,
                    color = Color(0xFFF5F0E8)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Discover, collect & track every\nbook that moves you.",
                    fontSize = 17.sp,
                    lineHeight = 26.sp,
                    color = Color(0xFF9A9A9A)
                )

                Spacer(modifier = Modifier.weight(0.5f))

                // Horizontal accent line
                HorizontalDivider(
                    color = Color(0xFFD4A853).copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(32.dp))

                // CTA Buttons
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4A853),
                        contentColor = Color(0xFF0D0D0D)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "Begin Your Journey",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFF5F0E8)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF6B6B6B)
                    )
                ) {
                    Text(
                        "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
