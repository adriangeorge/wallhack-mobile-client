package com.example.wallhacks.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallhacks.data.LocalizationResult
import com.example.wallhacks.data.distanceM
import com.example.wallhacks.data.toLocalMeters
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.tan

private val MarkerColor = Color(0xFF00E676)

/** Assumed height of the camera above the ground; the server does not report it yet. */
private const val EYE_HEIGHT_M = 1.6

/** Reticle half-size is REF_HALF_DP at REF_DIST_M and shrinks with 1/distance, clamped to a readable range. */
private const val REF_DIST_M = 50.0
private const val REF_HALF_DP = 20f
private const val MIN_HALF_DP = 6f
private const val MAX_HALF_DP = 36f

/**
 * Draws a marker over the camera preview for every object that falls inside the camera frustum,
 * using a pinhole projection from the pose the server returned. The pose is static (from the
 * moment the picture was taken), so the markers only line up while the phone is held as it was then.
 */
@Composable
fun PoiOverlay(result: LocalizationResult, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val pose = result.pose

    Canvas(modifier = modifier) {
        val tanHalfH = tan(Math.toRadians(pose.horizontalFovDeg / 2))
        // Vertical FOV follows from the aspect ratio of the preview.
        val tanHalfV = tanHalfH * size.height / size.width
        val pitch = Math.toRadians(pose.pitchDeg)

        for (poi in result.objects) {
            val (east, north) = toLocalMeters(pose.latitude, pose.longitude, poi.latitude, poi.longitude)
            val dist = distanceM(east, north)
            if (dist < 1.0) continue

            var rel = Math.toDegrees(atan2(east, north)) - pose.headingDeg
            rel = ((rel + 540) % 360) - 180 // normalise to [-180, 180)
            if (rel > 90 || rel < -90) continue // behind the camera

            val ndcX = tan(Math.toRadians(rel)) / tanHalfH
            // Aim at the middle of the object, relative to the camera's eye height.
            val elevation = atan((poi.heightM / 2 - EYE_HEIGHT_M) / dist) - pitch
            val ndcY = tan(elevation) / tanHalfV
            if (ndcX < -1 || ndcX > 1 || ndcY < -1 || ndcY > 1) continue

            val pos = Offset(
                x = ((ndcX + 1) / 2 * size.width).toFloat(),
                y = ((1 - ndcY) / 2 * size.height).toFloat(),
            )

            val half = (REF_HALF_DP.dp.toPx() * (REF_DIST_M / dist).toFloat())
                .coerceIn(MIN_HALF_DP.dp.toPx(), MAX_HALF_DP.dp.toPx())
            val diamond = Path().apply {
                moveTo(pos.x, pos.y - half)
                lineTo(pos.x + half, pos.y)
                lineTo(pos.x, pos.y + half)
                lineTo(pos.x - half, pos.y)
                close()
            }
            drawPath(diamond, MarkerColor, style = Stroke(width = (half / 6f).coerceAtLeast(2.dp.toPx())))

            val layout = measurer.measure(
                text = "${poi.label}\n${dist.toInt()} m, ${poi.heightM} m tall",
                style = TextStyle(color = Color.White, fontSize = 13.sp),
            )
            val textPos = Offset(
                (pos.x - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
                (pos.y + half + 6.dp.toPx()).coerceAtMost(size.height - layout.size.height),
            )
            drawText(layout, topLeft = textPos, shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 6f))
        }
    }
}
