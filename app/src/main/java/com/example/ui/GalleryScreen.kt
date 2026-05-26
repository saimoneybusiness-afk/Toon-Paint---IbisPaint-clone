package com.example.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DrawingEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GalleryScreen(
    drawings: List<DrawingEntity>,
    onCreateNew: (String) -> Unit,
    onSelectDrawing: (Int) -> Unit,
    onDeleteDrawing: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newDrawingName by remember { mutableStateOf("") }
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BrutalistColors.OffWhite,
        floatingActionButton = {
            // Stylized Neo-Brutalist floating action button
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
                    .background(BrutalistColors.Black, RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(BrutalistColors.Pink, RoundedCornerShape(16.dp))
                        .border(3.dp, BrutalistColors.Black, RoundedCornerShape(16.dp))
                        .clickable {
                            newDrawingName = ""
                            showCreateDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create cartoon",
                        tint = BrutalistColors.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Playful Toon Paint Header
            BrutalistCard(
                backgroundColor = BrutalistColors.Yellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(BrutalistColors.Black, RoundedCornerShape(10.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = (-3).dp, y = (-3).dp)
                                .background(BrutalistColors.Cyan, RoundedCornerShape(10.dp))
                                .border(2.5.dp, BrutalistColors.Black, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Logo",
                                tint = BrutalistColors.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Toon Paint",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrutalistColors.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "An Ibis-Inspired Cartoon Drawing Suite",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrutalistColors.Black.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Drawings Grid
            if (drawings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BrutalistCard(
                        backgroundColor = BrutalistColors.Cyan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = "🎨 Empty Studio Stage!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrutalistColors.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your manga canvas gallery is waiting. Pop open a brand new high-energy illustration layer and paint rough outlines, stylized cartoon ink lines, and saturated colors!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrutalistColors.Black.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BrutalistButton(
                                onClick = {
                                    newDrawingName = ""
                                    showCreateDialog = true
                                },
                                backgroundColor = BrutalistColors.Pink
                            ) {
                                Text(
                                    text = "Start Creating Now",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "My Cartoon Portfolio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrutalistColors.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(drawings, key = { it.id }) { drawing ->
                        BrutalistCard(
                            backgroundColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDrawing(drawing.id) },
                            shadowOffset = 5.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Thumbnail Image
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(BrutalistColors.Grey, RoundedCornerShape(8.dp))
                                        .border(2.dp, BrutalistColors.Black, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = remember(drawing.previewBase64) {
                                        if (!drawing.previewBase64.isNullOrEmpty()) {
                                            try {
                                                val decodedBytes = Base64.decode(drawing.previewBase64, Base64.DEFAULT)
                                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        } else null
                                    }

                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Project Preview",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        // Default stylized background grid as placeholder
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "Toon Paint",
                                                color = BrutalistColors.Black.copy(0.4f),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Layered Vector",
                                                color = BrutalistColors.Black.copy(0.3f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Title & Date
                                Text(
                                    text = drawing.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrutalistColors.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = formatter.format(Date(drawing.updatedAt)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "EDIT ✏️",
                                        color = BrutalistColors.Pink,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .clickable { onSelectDrawing(drawing.id) }
                                            .border(
                                                BorderStroke(1.5.dp, BrutalistColors.Black),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .background(
                                                BrutalistColors.Yellow,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(BrutalistColors.Black, RoundedCornerShape(6.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .offset(x = (-2).dp, y = (-2).dp)
                                                .background(BrutalistColors.Orange, RoundedCornerShape(6.dp))
                                                .border(1.5.dp, BrutalistColors.Black, RoundedCornerShape(6.dp))
                                                .clickable { onDeleteDrawing(drawing.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Drawing",
                                                tint = BrutalistColors.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Stylized Neo-Brutalist Create Canvas Dialog
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            BrutalistCard(
                backgroundColor = BrutalistColors.Cyan,
                shadowColor = BrutalistColors.Black,
                shadowOffset = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Launch Cartoon Canvas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrutalistColors.Black
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Name your masterpiece to initialize the layered vector workspace.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalistColors.Black.copy(0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                BrutalistTextField(
                    value = newDrawingName,
                    onValueChange = { newDrawingName = it },
                    placeholder = "e.g., Superhero Inking"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "CANCEL",
                        color = BrutalistColors.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 24.dp)
                            .clickable { showCreateDialog = false }
                    )

                    BrutalistButton(
                        onClick = {
                            if (newDrawingName.isNotBlank()) {
                                onCreateNew(newDrawingName.trim())
                                showCreateDialog = false
                            }
                        },
                        backgroundColor = BrutalistColors.Yellow
                    ) {
                        Text(
                            text = "OPEN 🖌️",
                            color = BrutalistColors.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
