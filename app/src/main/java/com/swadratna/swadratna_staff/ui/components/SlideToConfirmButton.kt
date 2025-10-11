package com.swadratna.swadratna_staff.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * A generic composable that provides a 'slide to confirm' functionality.
 *
 * @param text The text prompt displayed inside the slider (e.g., "Slide to Confirm").
 * @param onConfirmation The action to execute when the slide is successfully completed.
 * @param trackColor The background color of the slider track.
 * @param thumbColor The color of the draggable thumb.
 * @param modifier The modifier for the overall container.
 */
@Composable
fun SlideToConfirm(
    text: String,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.primary, // Use theme color for generic
    thumbColor: Color = Color.White
) {
    var offsetX by remember { mutableStateOf(0f) }
    var containerWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val completeThreshold = 0.6f
    val thumbSizeDp = 50.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    val maxDragPx = remember(containerWidth) {
        if (containerWidth > 0) containerWidth - thumbSizePx else 0f
    }

    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(0f, maxDragPx)

        // Confirmation Logic
        if (offsetX >= maxDragPx * completeThreshold) {
            onConfirmation()
            // Reset position immediately after confirmation
            offsetX = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSizeDp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .onSizeChanged { containerWidth = it.width }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    // Snap back to the start if not confirmed
                    if (offsetX < maxDragPx * completeThreshold) {
                        offsetX = 0f
                    }
                }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Text Hint (Fades out as the thumb progresses)
        val progress = if (maxDragPx > 0) (offsetX / maxDragPx).coerceIn(0f, 1f) else 0f
        val textAlpha = (1f - progress * 1.5f).coerceIn(0f, 1f)

        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = textAlpha),
            modifier = Modifier.align(Alignment.Center)
        )

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(x = offsetX.roundToInt(), y = 0) }
                .size(thumbSizeDp)
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(thumbColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                ">",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = trackColor // Thumb text color matches the track
            )
        }
    }
}