package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Base64
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.*
import java.io.ByteArrayOutputStream

// Generates smooth Bézier path from points sequence
fun createSmoothPath(points: List<ToonPoint>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size < 3) {
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        return path
    }
    
    path.moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 2) {
        val p1 = points[i + 1]
        val p2 = points[i + 2]
        
        val xc = (p1.x + p2.x) / 2
        val yc = (p1.y + p2.y) / 2
        path.quadraticTo(p1.x, p1.y, xc, yc)
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}

@Composable
fun ToonCanvas(
    layers: List<ToonLayer>,
    activeLayerId: String,
    activeStroke: ToonStroke?,
    paperStyle: String,
    onDragStart: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(activeLayerId) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onDrag(change.position.x, change.position.y)
                    },
                    onDragEnd = {
                        onDragEnd()
                    }
                )
            }
    ) {
        // 1. Draw Paper Background Style
        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            when (paperStyle) {
                "White" -> {
                    drawRect(color = Color.White)
                }
                "Manga Grid" -> {
                    drawRect(color = Color.White)
                    val step = 60f
                    // Vertical grid
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFFE0F7FA),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 2f
                        )
                        x += step
                    }
                    // Horizontal grid
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFFE0F7FA),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f
                        )
                        y += step
                    }
                }
                else -> {
                    // Transparent checkerboard
                    val squareSize = 40f
                    var y = 0f
                    while (y < size.height) {
                        var x = 0f
                        var isAlt = (y / squareSize).toInt() % 2 == 0
                        while (x < size.width) {
                            drawRect(
                                color = if (isAlt) Color(0xFFE0E0E0) else Color(0xFFF5F5F5),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                            )
                            isAlt = !isAlt
                            x += squareSize
                        }
                        y += squareSize
                    }
                }
            }
        }

        // 2. Render Layers Stack (Bottom-most to Top-most, meaning reverse order of layers index)
        for (i in layers.indices.reversed()) {
            val layer = layers[i]
            if (!layer.visible) continue

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Offscreen compositing handles transparency in layers and transparency masks (eraser BlendMode.Clear) beautifully
                        alpha = layer.opacity
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                    // Draw committed layer strokes
                    for (stroke in layer.strokes) {
                        if (stroke.points.isEmpty()) continue
                        
                        if (stroke.points.size == 1) {
                            val pt = stroke.points[0]
                            if (stroke.isEraser) {
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = stroke.size / 2f,
                                    center = Offset(pt.x, pt.y),
                                    blendMode = BlendMode.Clear
                                )
                            } else {
                                drawCircle(
                                    color = Color(stroke.color),
                                    radius = stroke.size / 2f,
                                    center = Offset(pt.x, pt.y)
                                )
                            }
                        } else {
                            val path = createSmoothPath(stroke.points)
                            if (stroke.isEraser) {
                                drawPath(
                                    path = path,
                                    color = Color.Transparent,
                                    style = Stroke(
                                        width = stroke.size,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    ),
                                    blendMode = BlendMode.Clear
                                )
                            } else {
                                drawPath(
                                    path = path,
                                    color = Color(stroke.color),
                                    style = Stroke(
                                        width = stroke.size,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }

                    // Render active, uncommitted real-time stroke on top of the selected active layer
                    if (layer.id == activeLayerId && activeStroke != null && activeStroke.points.isNotEmpty()) {
                        if (activeStroke.points.size == 1) {
                            val pt = activeStroke.points[0]
                            if (activeStroke.isEraser) {
                                drawCircle(
                                    color = Color.Transparent,
                                    radius = activeStroke.size / 2f,
                                    center = Offset(pt.x, pt.y),
                                    blendMode = BlendMode.Clear
                                )
                            } else {
                                drawCircle(
                                    color = Color(activeStroke.color),
                                    radius = activeStroke.size / 2f,
                                    center = Offset(pt.x, pt.y)
                                )
                            }
                        } else {
                            val path = createSmoothPath(activeStroke.points)
                            if (activeStroke.isEraser) {
                                drawPath(
                                    path = path,
                                    color = Color.Transparent,
                                    style = Stroke(
                                        width = activeStroke.size,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    ),
                                    blendMode = BlendMode.Clear
                                )
                            } else {
                                drawPath(
                                    path = path,
                                    color = Color(activeStroke.color),
                                    style = Stroke(
                                        width = activeStroke.size,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Global High-fidelity Vector to Bitmap Renderer
fun renderToBitmap(
    layers: List<ToonLayer>,
    width: Int,
    height: Int,
    paperStyle: String
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw grid/background
    if (paperStyle == "White") {
        canvas.drawColor(android.graphics.Color.WHITE)
    } else if (paperStyle == "Manga Grid") {
        canvas.drawColor(android.graphics.Color.WHITE)
        val gridPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E0F7FA")
            strokeWidth = 2f
            isAntiAlias = true
        }
        val step = 60f
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += step
        }
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += step
        }
    } else {
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    // Process from bottom (last element) to top (first element)
    for (i in layers.indices.reversed()) {
        val layer = layers[i]
        if (!layer.visible) continue

        val layerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val layerCanvas = Canvas(layerBitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            style = Paint.Style.STROKE
        }

        for (stroke in layer.strokes) {
            if (stroke.points.isEmpty()) continue
            paint.strokeWidth = stroke.size
            if (stroke.isEraser) {
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            } else {
                paint.xfermode = null
                paint.color = stroke.color
            }

            if (stroke.points.size == 1) {
                paint.style = Paint.Style.FILL
                val pt = stroke.points[0]
                layerCanvas.drawCircle(pt.x, pt.y, stroke.size / 2f, paint)
                paint.style = Paint.Style.STROKE
            } else {
                val path = android.graphics.Path()
                val points = stroke.points
                path.moveTo(points[0].x, points[0].y)
                if (points.size < 3) {
                    for (k in 1 until points.size) {
                        path.lineTo(points[k].x, points[k].y)
                    }
                } else {
                    for (k in 0 until points.size - 2) {
                        val p1 = points[k + 1]
                        val p2 = points[k + 2]
                        val xc = (p1.x + p2.x) / 2
                        val yc = (p1.y + p2.y) / 2
                        path.quadTo(p1.x, p1.y, xc, yc)
                    }
                    path.lineTo(points.last().x, points.last().y)
                }
                layerCanvas.drawPath(path, paint)
            }
        }

        // Composite the layer with its custom opacity
        val filterPaint = Paint().apply {
            isAntiAlias = true
            alpha = (layer.opacity * 255).toInt()
        }
        canvas.drawBitmap(layerBitmap, 0f, 0f, filterPaint)
    }

    return bitmap
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}
