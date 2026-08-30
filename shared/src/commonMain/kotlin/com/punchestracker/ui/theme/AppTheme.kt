package com.punchestracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Color(0xFFB45A7A),
    onPrimary = Color.White,
    secondary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        content = content,
    )
}
