package com.swadratna.swadratna_staff.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swadratna.swadratna_staff.ui.theme.Red40
import com.swadratna.swadratna_staff.ui.theme.Red80
import kotlin.math.roundToInt

/**
 * A custom composable that acts as a "Slide to Confirm" button.
 *
 * @param onConfirmOrder The action to execute when the slide is successfully completed.
 * @param modifier The modifier for the overall button container.
 */
@Composable
fun SlideToConfirmButton(
    onConfirmOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. STATE MANAGEMENT
    var offsetX by remember { mutableStateOf(0f) }
    var containerWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    // A threshold for a successful slide (e.g., 80% of the width)
    val completeThreshold = 0.8f

    // Convert dp to pixels for the thumb width (adjust as needed)
    val thumbSizeDp = 50.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    // Calculate the maximum drag distance
    val maxDragPx = remember(containerWidth) {
        if (containerWidth > 0) containerWidth - thumbSizePx else 0f
    }

    // 2. DRAG GESTURE HANDLER
    val draggableState = rememberDraggableState { delta ->
        offsetX = (offsetX + delta).coerceIn(0f, maxDragPx)

        // Check for confirmation when dragging
        if (offsetX >= maxDragPx * completeThreshold) {
            onConfirmOrder()
            // Reset position after confirmation, or handle a confirmed state transition
            offsetX = 0f 
        }
    }

    // 3. UI LAYOUT
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Red80) // Green background
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
        // Text Hint (Center-aligned)
        // Adjust alpha based on slide progress
        val progress = if (maxDragPx > 0) (offsetX / maxDragPx).coerceIn(0f, 1f) else 0f
        val textAlpha = (1f - progress * 1.5f).coerceIn(0f, 1f) // Fades out faster

        Text(
            "Slide to Confirm Order",
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
                .padding(4.dp) // Inner padding for a border effect
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White), // White thumb
            contentAlignment = Alignment.Center
        ) {
            Text(
                ">",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Red80
            )
        }
    }
}

@Preview
@Composable
fun ComposerPreview() {
    SlideToConfirmButton(
        onConfirmOrder = {},

    )
}