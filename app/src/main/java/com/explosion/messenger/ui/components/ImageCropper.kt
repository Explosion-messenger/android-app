package com.explosion.messenger.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.explosion.messenger.ui.theme.AccentBlue
import com.explosion.messenger.ui.theme.BgDark
import com.explosion.messenger.ui.theme.TextWhite
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

@Composable
fun CircularCropperDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onCropped: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(uri) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val loadedBitmap = BitmapFactory.decodeStream(inputStream)
        bitmap = loadedBitmap?.asImageBitmap()
    }

    if (bitmap != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = BgDark
            ) {
                Column {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        CropperContent(bitmap!!, onCropped)
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL", color = TextWhite)
                        }
                        // Confirmation is handled in CropperContent for simplicity or via a button
                    }
                }
            }
        }
    }
}

@Composable
fun CropperContent(
    imageBitmap: ImageBitmap,
    onCropped: (Bitmap) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val imageWidth = imageBitmap.width.toFloat()
    val imageHeight = imageBitmap.height.toFloat()

    BoxWithConstraints {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        
        // Define crop area (circle)
        val cropSize = min(containerWidth, containerHeight) * 0.9f
        
        // Limits
        val baseScale = max(cropSize / imageWidth, cropSize / imageHeight)
        val minScale = 1f
        val maxScale = 5f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(minScale, maxScale)
                        
                        val totalScale = baseScale * scale
                        val scaledWidth = imageWidth * totalScale
                        val scaledHeight = imageHeight * totalScale

                        val maxX = max(0f, (scaledWidth - containerWidth) / 2)
                        val maxY = max(0f, (scaledHeight - containerHeight) / 2)

                        val newX = (offset.x + pan.x).coerceIn(-maxX, maxX)
                        val newY = (offset.y + pan.y).coerceIn(-maxY, maxY)

                        offset = Offset(newX, newY)
                    }
                }
        ) {
            val cropRect = Rect(
                offset = Offset((containerWidth - cropSize) / 2, (containerHeight - cropSize) / 2),
                size = Size(cropSize, cropSize)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw image with transform
                val totalScale = baseScale * scale
                
                val scaledWidth = imageWidth * totalScale
                val scaledHeight = imageHeight * totalScale
                
                val centerOffset = Offset(
                    (containerWidth - scaledWidth) / 2 + offset.x,
                    (containerHeight - scaledHeight) / 2 + offset.y
                )

                drawImage(
                    image = imageBitmap,
                    dstOffset = IntOffset(centerOffset.x.toInt(), centerOffset.y.toInt()),
                    dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
                )

                // Draw overlay mask
                val path = Path().apply {
                    addRect(Rect(0f, 0f, containerWidth, containerHeight))
                    addOval(cropRect)
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path, Color.Black.copy(alpha = 0.7f))
                
                // Draw circle border
                drawCircle(
                    color = AccentBlue,
                    radius = cropSize / 2,
                    center = cropRect.center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

        Button(
            onClick = {
                // Logic to crop the bitmap
                val baseScale = max(cropSize / imageWidth, cropSize / imageHeight)
                val totalScale = baseScale * scale
                
                val renderBitmap = Bitmap.createBitmap(cropSize.toInt(), cropSize.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(renderBitmap)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                
                // Matrix for cropping
                val matrix = Matrix()
                matrix.postScale(totalScale, totalScale)
                matrix.postTranslate(
                    (containerWidth - imageWidth * totalScale) / 2 + offset.x - (containerWidth / 2 - cropSize / 2),
                    (containerHeight - imageHeight * totalScale) / 2 + offset.y - (containerHeight / 2 - cropSize / 2)
                )
                
                canvas.drawBitmap(imageBitmap.asAndroidBitmap(), matrix, paint)
                
                onCropped(renderBitmap)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text("SET PROFILE PHOTO", fontWeight = FontWeight.Black)
        }
    }
}

}
