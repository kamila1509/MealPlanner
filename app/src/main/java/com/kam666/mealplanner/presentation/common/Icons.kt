package com.kam666.mealplanner.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun DrawableForkBowlIcon(modifier: Modifier = Modifier) {
    val white = Color.White

    Canvas(modifier = modifier) {
        val size = this.size.width
        val scale = size / 100f

        // Fork: 3 tines
        drawRoundRect(
            color = white,
            topLeft = Offset(24f * scale, 15f * scale),
            size = androidx.compose.ui.geometry.Size(6f * scale, 27f * scale),
            cornerRadius = CornerRadius(3f * scale),
        )

        drawRoundRect(
            color = white,
            topLeft = Offset(33f * scale, 15f * scale),
            size = androidx.compose.ui.geometry.Size(6f * scale, 27f * scale),
            cornerRadius = CornerRadius(3f * scale),
        )

        drawRoundRect(
            color = white,
            topLeft = Offset(42f * scale, 15f * scale),
            size = androidx.compose.ui.geometry.Size(6f * scale, 27f * scale),
            cornerRadius = CornerRadius(3f * scale),
        )

        // Fork: bridge
        drawRoundRect(
            color = white,
            topLeft = Offset(24f * scale, 40f * scale),
            size = androidx.compose.ui.geometry.Size(24f * scale, 7f * scale),
            cornerRadius = CornerRadius(2.5f * scale),
        )

        // Fork: handle
        drawRoundRect(
            color = white,
            topLeft = Offset(30f * scale, 44f * scale),
            size = androidx.compose.ui.geometry.Size(12f * scale, 38f * scale),
            cornerRadius = CornerRadius(6f * scale),
        )

        // Bowl: outer ring
        drawCircle(
            color = white,
            radius = 20f * scale,
            center = Offset(67f * scale, 64f * scale),
            style = Stroke(width = 4f * scale)
        )

        // Bowl: subtle inner circle
        drawCircle(
            color = white,
            radius = 13f * scale,
            center = Offset(67f * scale, 64f * scale),
            alpha = 0.1f
        )

        // Steam wisps (curved lines)
        drawSteamWisp(Offset(59f * scale, 42f * scale), Offset(59f * scale, 17f * scale), white, scale)
        drawSteamWisp(Offset(67f * scale, 40f * scale), Offset(67f * scale, 15f * scale), white, scale)
        drawSteamWisp(Offset(75f * scale, 42f * scale), Offset(75f * scale, 17f * scale), white, scale)
    }
}

private fun DrawScope.drawSteamWisp(start: Offset, end: Offset, color: Color, scale: Float) {
    val path = Path().apply {
        moveTo(start.x, start.y)
        // Curved line: quadratic bezier
        val controlX = start.x - 6f * scale
        val controlY = (start.y + end.y) / 2f
        quadraticBezierTo(controlX, controlY, end.x, end.y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
    )
}
