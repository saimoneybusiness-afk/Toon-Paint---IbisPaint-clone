package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.ToonLayer
import com.example.data.ToonStroke
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    title: String,
    layers: List<ToonLayer>,
    activeLayerId: String,
    activeStroke: ToonStroke?,
    activeBrushColor: Int,
    activeBrushSize: Float,
    isEraserMode: Boolean,
    currentPaletteIndex: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBrushColorChange: (Int) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onEraserModeChange: (Boolean) -> Unit,
    onPaletteIndexChange: (Int) -> Unit,
    onAddLayer: () -> Unit,
    onDeleteLayer: (String) -> Unit,
    onReorderLayers: (Int, Int) -> Unit,
    onLayerVisibilityChange: (String, Boolean) -> Unit,
    onLayerOpacityChange: (String, Float) -> Unit,
    onSelectActiveLayer: (String) -> Unit,
    onClearActiveLayer: () -> Unit,
    onDragStart: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onSaveThumbnail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var paperStyle by remember { mutableStateOf("White") } // White, Manga Grid, Transparent
    var showLayersPanel by remember { mutableStateOf(false) }
    var showBrushSettings by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(title) }

    // Curated Playful Palettes
    val SaturdayCartoon = listOf(
        0xFF000000.toInt(), // Ink
        0xFFFFFFFF.toInt(), // Shine
        0xFFFF3E3E.toInt(), // Tomato Red
        0xFFFFD900.toInt(), // Sunshine Yellow
        0xFF00E5FF.toInt(), // Cyan
        0xFFFF6C00.toInt(), // Neon Orange
        0xFF00C853.toInt(), // Green
        0xFF9D00FF.toInt(), // Purple
        0xFFFF4081.toInt()  // Pink
    )

    val RetroPopArt = listOf(
        0xFF1A1A1A.toInt(), // Obsidian
        0xFFFFFEF4.toInt(), // Canvas Ivory
        0xFFFF1744.toInt(), // Electric Crimson
        0xFFFFEA00.toInt(), // Banana Yellow
        0xFF1DE9B6.toInt(), // Retro Mint
        0xFF2979FF.toInt(), // Cobalt Blue
        0xFFFF9100.toInt(), // Orange Soda
        0xFFD500F9.toInt(), // Fuchsia Punk
        0xFF76FF03.toInt()  // Acid Lime
    )

    val MangaNoir = listOf(
        0xFF121212.toInt(), // Comic Black
        0xFFFFFDE7.toInt(), // Aged Paper
        0xFF424242.toInt(), // Dark Tone
        0xFF757575.toInt(), // Tone 50
        0xFFBDBDBD.toInt(), // Tone 20
        0xFFE0F7FA.toInt(), // Blue Tint
        0xFFE53935.toInt(), // Accent Red
        0xFF0D47A1.toInt(), // Inking Blue
        0xFF5D4037.toInt()  // Sepia
    )

    val paletteList = listOf(SaturdayCartoon, RetroPopArt, MangaNoir)
    val paletteNames = listOf("Saturday Cartoons", "Retro Pop-Art", "Manga Noir")
    val activePalette = paletteList[currentPaletteIndex]

    // Capture the generated drawing to save as thumbnail reactively after strokes
    LaunchedEffect(layers) {
        // Build small thumbnail to write back (200x200 scale is fast and lightweight)
        try {
            val thumb = renderToBitmap(layers, 200, 200, paperStyle)
            val base64 = bitmapToBase64(thumb)
            onSaveThumbnail(base64)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrutalistColors.OffWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. TOP UTILITY BAR (Neo-Brutalist Frame)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(2.dp, BrutalistColors.Black)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Key Button
                    BrutalistIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        backgroundColor = BrutalistColors.Yellow,
                        size = 40.dp,
                        shadowOffset = 2.dp
                    )

                    // Title edit tap
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .border(1.5.dp, BrutalistColors.Black, RoundedCornerShape(6.dp))
                            .background(BrutalistColors.Grey, RoundedCornerShape(6.dp))
                            .clickable {
                                editedTitle = title
                                showTitleDialog = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrutalistColors.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit name",
                            tint = BrutalistColors.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Drawing Undo / Redo Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrutalistIconButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            onClick = onUndo,
                            backgroundColor = if (canUndo) BrutalistColors.Cyan else Color.LightGray,
                            size = 40.dp,
                            shadowOffset = 2.dp
                        )

                        BrutalistIconButton(
                            icon = Icons.AutoMirrored.Filled.Redo,
                            onClick = onRedo,
                            backgroundColor = if (canRedo) BrutalistColors.Pink else Color.LightGray,
                            size = 40.dp,
                            shadowOffset = 2.dp
                        )

                        // Studio Export Button
                        BrutalistIconButton(
                            icon = Icons.Default.IosShare,
                            onClick = { showExportDialog = true },
                            backgroundColor = BrutalistColors.Lime,
                            size = 40.dp,
                            shadowOffset = 2.dp
                        )
                    }
                }
            }

            // 2. LAYER CONTROLS FLOATING HEADER SUMMARY
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalistColors.Yellow)
                    .border(BorderStroke(1.dp, BrutalistColors.Black))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeLayerName = layers.find { it.id == activeLayerId }?.name ?: "Unknown"
                Text(
                    text = "✎ ACTIVE LAYER: $activeLayerName",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrutalistColors.Black
                )
                Text(
                    text = "Layers Stack: ${layers.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrutalistColors.Black,
                    modifier = Modifier
                        .clickable { showLayersPanel = true }
                        .border(1.dp, BrutalistColors.Black, RoundedCornerShape(4.dp))
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // 3. MAIN SMOOTH CANVAS WRAPPER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.DarkGray)
            ) {
                ToonCanvas(
                    layers = layers,
                    activeLayerId = activeLayerId,
                    activeStroke = activeStroke,
                    paperStyle = paperStyle,
                    onDragStart = onDragStart,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. THE LOWER UTILITY BAR (Palettes, Size sliders, Tool triggers)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(2.dp, BrutalistColors.Black)
                    .padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    
                    // A. Color Palette Slider + Palette Category Toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small Palette Swapper Button
                        Box(
                            modifier = Modifier
                                .border(2.dp, BrutalistColors.Black, RoundedCornerShape(6.dp))
                                .background(BrutalistColors.Yellow, RoundedCornerShape(6.dp))
                                .clickable {
                                    val nextIdx = (currentPaletteIndex + 1) % paletteNames.size
                                    onPaletteIndexChange(nextIdx)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🎨 ${paletteNames[currentPaletteIndex].split(" ").last()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = BrutalistColors.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Scrollable vibrant color swatches
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(end = 12.dp)
                        ) {
                            items(activePalette) { colInt ->
                                val isSelected = activeBrushColor == colInt && !isEraserMode
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .shadow(if (isSelected) 3.dp else 0.dp, CircleShape)
                                        .background(Color(colInt), CircleShape)
                                        .border(
                                            width = if (isSelected) 3f.dp else 1.5.dp,
                                            color = if (isSelected) BrutalistColors.Pink else BrutalistColors.Black,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onEraserModeChange(false)
                                            onBrushColorChange(colInt)
                                        }
                                )
                            }
                        }
                    }

                    // B. Drawer Controls Panel triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brush vs Eraser Toggle Widget
                        Row(
                            modifier = Modifier.border(2.dp, BrutalistColors.Black, RoundedCornerShape(8.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Brush Tool
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (!isEraserMode) BrutalistColors.Cyan else Color.White,
                                        RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                                    )
                                    .clickable { onEraserModeChange(false) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = "Brush Mode",
                                    tint = BrutalistColors.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Line separator
                            Box(modifier = Modifier.width(2.dp).height(36.dp).background(BrutalistColors.Black))

                            // Eraser Tool (Alpha Eraser Mechanics)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isEraserMode) BrutalistColors.Pink else Color.White,
                                        RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                                    )
                                    .clickable { onEraserModeChange(true) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = "Eraser Mode",
                                    tint = BrutalistColors.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Brush Settings Slider Box Toggler
                        BrutalistButton(
                            onClick = { showBrushSettings = !showBrushSettings },
                            backgroundColor = BrutalistColors.Yellow,
                            modifier = Modifier.height(38.dp),
                            shadowOffset = 2.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Sizing slider",
                                tint = BrutalistColors.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Size: ${activeBrushSize.toInt()}px",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrutalistColors.Black
                            )
                        }

                        // Grid background style cyclist
                        BrutalistButton(
                            onClick = {
                                paperStyle = when (paperStyle) {
                                    "White" -> "Manga Grid"
                                    "Manga Grid" -> "Checkerboard"
                                    else -> "White"
                                }
                            },
                            backgroundColor = BrutalistColors.Orange,
                            modifier = Modifier.height(38.dp),
                            shadowOffset = 2.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = "Paper style",
                                tint = BrutalistColors.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paper",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrutalistColors.Black
                            )
                        }

                        // Floating Layer Manager clicker
                        BrutalistButton(
                            onClick = { showLayersPanel = true },
                            backgroundColor = BrutalistColors.Cyan,
                            modifier = Modifier.height(38.dp),
                            shadowOffset = 2.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Layers Manager",
                                tint = BrutalistColors.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LAYERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrutalistColors.Black
                            )
                        }
                    }
                }
            }
        }

        // 5. BRUSH SETTINGS PULL-OUT EXPANDER BOX (Brush properties adjust)
        if (showBrushSettings) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp, start = 12.dp, end = 12.dp)
            ) {
                BrutalistCard(
                    backgroundColor = Color.White,
                    shadowOffset = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Brush Size Controller",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrutalistColors.Black
                        )
                        IconButton(onClick = { showBrushSettings = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "close",
                                tint = Color.Gray
                            )
                        }
                    }

                    // Stroke size slide
                    Slider(
                        value = activeBrushSize,
                        onValueChange = onBrushSizeChange,
                        valueRange = 2f..72f,
                        colors = SliderDefaults.colors(
                            thumbColor = BrutalistColors.Pink,
                            activeTrackColor = BrutalistColors.Black,
                            inactiveTrackColor = Color.LightGray
                        )
                    )

                    // Real-time brush round blob preview
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Brush Dot Preview: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(activeBrushSize.dp)
                                .clip(CircleShape)
                                .background(if (isEraserMode) Color.Transparent else Color(activeBrushColor))
                                .border(
                                    width = if (isEraserMode) 1.5.dp else 0.dp,
                                    color = if (isEraserMode) Color.Red else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        // 6. FLOATING NEU-BRUTALIST LAYER MANAGER SCREEN SHEET
        if (showLayersPanel) {
            Dialog(onDismissRequest = { showLayersPanel = false }) {
                BrutalistCard(
                    backgroundColor = BrutalistColors.Cyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shadowOffset = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Layer Stack Master",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrutalistColors.Black
                        )
                        IconButton(onClick = { showLayersPanel = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "close",
                                tint = BrutalistColors.Black
                            )
                        }
                    }

                    Text(
                        text = "Stack order determines render overlapping. Top cards render above bottom cards in the illustration canvas.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalistColors.Black.copy(0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Add Layer Button
                    BrutalistButton(
                        onClick = onAddLayer,
                        backgroundColor = BrutalistColors.Yellow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New layer",
                            tint = BrutalistColors.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADD NEW TRANSPARENT LAYER",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = BrutalistColors.Black
                        )
                    }

                    // Scrolled Layers List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        layers.forEachIndexed { index, layer ->
                            val isActive = layer.id == activeLayerId
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isActive) 2.5.dp else 1.5.dp,
                                        color = if (isActive) BrutalistColors.Pink else BrutalistColors.Black,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isActive) BrutalistColors.Yellow else Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectActiveLayer(layer.id) }
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Selected layer tick indicator
                                            if (isActive) {
                                                Text(
                                                    text = "★ ",
                                                    color = BrutalistColors.Pink,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            Text(
                                                text = layer.name,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = BrutalistColors.Black
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Visibility Toggle
                                            IconButton(
                                                onClick = {
                                                    onLayerVisibilityChange(layer.id, !layer.visible)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (layer.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "visibility",
                                                    tint = BrutalistColors.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Reorder UP
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        onReorderLayers(index, index - 1)
                                                    }
                                                },
                                                enabled = index > 0,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowUpward,
                                                    contentDescription = "move up",
                                                    tint = if (index > 0) BrutalistColors.Black else Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Reorder DOWN
                                            IconButton(
                                                onClick = {
                                                    if (index < layers.size - 1) {
                                                        onReorderLayers(index, index + 1)
                                                    }
                                                },
                                                enabled = index < layers.size - 1,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = "move down",
                                                    tint = if (index < layers.size - 1) BrutalistColors.Black else Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Delete Layer (minimum 1 layer protection)
                                            IconButton(
                                                onClick = { onDeleteLayer(layer.id) },
                                                enabled = layers.size > 1,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "delete",
                                                    tint = if (layers.size > 1) Color.Red else Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Local opacity slider for this layer
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Opacity: ${(layer.opacity * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray,
                                            modifier = Modifier.width(80.dp)
                                        )
                                        Slider(
                                            value = layer.opacity,
                                            onValueChange = { onLayerOpacityChange(layer.id, it) },
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = BrutalistColors.Pink,
                                                activeTrackColor = BrutalistColors.Black
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Empty Ink Strokes Clear Button
                    BrutalistButton(
                        onClick = {
                            onClearActiveLayer()
                            Toast.makeText(context, "Active Layer Ink Cleared!", Toast.LENGTH_SHORT).show()
                        },
                        backgroundColor = BrutalistColors.Orange,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💣 CLEAR CURRENT LAYER INK",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 7. ANIME STUDIO EXPORT SUCCESS INTERACTIVE POSTER DIALOG
        if (showExportDialog) {
            Dialog(onDismissRequest = { showExportDialog = false }) {
                BrutalistCard(
                    backgroundColor = BrutalistColors.Yellow,
                    shadowOffset = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "✨ TOON EXPORT STUDIO ✨",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrutalistColors.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Generated High-Res Card Image render
                    val previewBitmap = remember {
                        // Render at full double resolution (e.g. 600x600 px) for crisp artwork
                        renderToBitmap(layers, 600, 600, paperStyle)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .border(3.dp, BrutalistColors.Black, RoundedCornerShape(10.dp))
                            .background(Color.White, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Finished illustration preview",
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Vintage Retro Anime Stamp Seal
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .border(1.5.dp, Color.Red, RoundedCornerShape(4.dp))
                                .background(Color.White.copy(0.9f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TOON PAINT CO.",
                                color = Color.Red,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Poster Art Saved & Compressed!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrutalistColors.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Prepared successfully as PNG asset ($title). Click below to share your digital creation instantly across channels!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrutalistColors.Black.copy(0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Close Option
                        BrutalistButton(
                            onClick = { showExportDialog = false },
                            backgroundColor = BrutalistColors.OffWhite,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "CLOSE",
                                fontWeight = FontWeight.Bold,
                                color = BrutalistColors.Black,
                                fontSize = 12.sp
                            )
                        }

                        // Trigger Sharing Option
                        BrutalistButton(
                            onClick = {
                                try {
                                    // Save file to cache and initiate platform share intent
                                    val cacheDir = context.cacheDir
                                    val artFile = File(cacheDir, "${title.replace(" ", "_")}.png")
                                    val stream = FileOutputStream(artFile)
                                    previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                    stream.flush()
                                    stream.close()

                                    val authority = "${context.packageName}.fileprovider"
                                    val contentUri: Uri = FileProvider.getUriForFile(context, authority, artFile)

                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, contentUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Cartoon!"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error saving PNG to file system: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            backgroundColor = BrutalistColors.Pink,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SHARE 🚀",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 8. TITLE EDIT DIALOG BOX
        if (showTitleDialog) {
            Dialog(onDismissRequest = { showTitleDialog = false }) {
                BrutalistCard(
                    backgroundColor = BrutalistColors.Cyan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Edit Project Name",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrutalistColors.Black
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    BrutalistTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 20.dp)
                                .clickable { showTitleDialog = false }
                        )

                        BrutalistButton(
                            onClick = {
                                if (editedTitle.isNotBlank()) {
                                    onTitleChange(editedTitle.trim())
                                    showTitleDialog = false
                                }
                            },
                            backgroundColor = BrutalistColors.Yellow
                        ) {
                            Text(
                                text = "RENAME",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
