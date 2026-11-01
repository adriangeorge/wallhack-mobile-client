package com.example.wallhacks.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.wallhacks.data.LocalizationResult
import com.example.wallhacks.data.distanceM
import com.example.wallhacks.data.toLocalMeters
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private val InViewColor = Color(0xFF2E7D32)
private val OutsideColor = Color(0xFF757575)
private val CameraColor = Color(0xFF1565C0)

@Composable
fun MapScreen(result: LocalizationResult?, modifier: Modifier = Modifier) {
    if (result == null) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
            Text("No data yet. Take a picture on the Camera tab first.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        PlanView(result, Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp))

        val pose = result.pose
        Text(
            text = "Camera: %.5f, %.5f  alt %.0f m  heading %.0f°  FOV %.0f°".format(
                pose.latitude, pose.longitude, pose.altitudeM, pose.headingDeg, pose.horizontalFovDeg,
            ),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(result.objects, key = { it.id }) { poi ->
                Text(
                    text = "${poi.label} (${if (poi.inFieldOfView) "in view" else "outside view"}): " +
                        "%.5f, %.5f, height %.1f m".format(poi.latitude, poi.longitude, poi.heightM),
                    color = if (poi.inFieldOfView) InViewColor else OutsideColor,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

/** Top-down plan view, north up, camera at the center. Stands in for a real map until we pick a map SDK. */
@Composable
private fun PlanView(result: LocalizationResult, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val pose = result.pose

    val local = result.objects.map { poi ->
        poi to toLocalMeters(pose.latitude, pose.longitude, poi.latitude, poi.longitude)
    }
    // Radius in meters shown from center to edge, with a little margin.
    val rangeM = max(50.0, (local.maxOfOrNull { (_, p) -> distanceM(p.first, p.second) } ?: 0.0) * 1.25)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val pxPerM = (size.minDimension / 2 / rangeM).toFloat()

        drawCircle(gridColor, radius = size.minDimension / 2, center = center, style = Stroke(2f))
        drawCircle(gridColor, radius = size.minDimension / 4, center = center, style = Stroke(1f))
        drawLabel(measurer, "N", Offset(center.x, 4f), textColor)
        drawLabel(measurer, "${rangeM.toInt()} m", Offset(center.x, 4f + 20f), gridColor)

        // Field-of-view wedge: heading is clockwise from north, canvas angle 0 is +x, so subtract 90.
        val half = pose.horizontalFovDeg / 2
        val radius = size.minDimension / 2
        val wedge = Path().apply {
            moveTo(center.x, center.y)
            for (edge in listOf(pose.headingDeg - half, pose.headingDeg + half)) {
                val a = Math.toRadians(edge - 90)
                lineTo(center.x + radius * cos(a).toFloat(), center.y + radius * sin(a).toFloat())
            }
            close()
        }
        drawPath(wedge, CameraColor.copy(alpha = 0.15f))

        // Camera marker: a triangle pointing along the heading.
        rotate(degrees = pose.headingDeg.toFloat(), pivot = center) {
            val tri = Path().apply {
                moveTo(center.x, center.y - 18f)
                lineTo(center.x - 11f, center.y + 10f)
                lineTo(center.x + 11f, center.y + 10f)
                close()
            }
            drawPath(tri, CameraColor)
        }

        for ((poi, p) in local) {
            val pos = Offset(center.x + (p.first * pxPerM).toFloat(), center.y - (p.second * pxPerM).toFloat())
            val color = if (poi.inFieldOfView) InViewColor else OutsideColor
            drawCircle(color, radius = 12f, center = pos)
            drawLabel(measurer, "${poi.label} ${poi.heightM}m", pos + Offset(0f, 16f), color)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLabel(
    measurer: TextMeasurer,
    text: String,
    anchor: Offset,
    color: Color,
) {
    val layout = measurer.measure(text)
    val topLeft = Offset(
        (anchor.x - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
        anchor.y.coerceIn(0f, size.height - layout.size.height),
    )
    drawText(layout, color = color, topLeft = topLeft)
}
