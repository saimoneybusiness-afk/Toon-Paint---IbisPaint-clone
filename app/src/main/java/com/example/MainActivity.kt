package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DrawingScreen
import com.example.ui.GalleryScreen
import com.example.ui.ToonViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    val viewModel: ToonViewModel = viewModel()
                    val drawings by viewModel.allDrawings.collectAsState()
                    val currentId by viewModel.currentDrawingId.collectAsState()
                    val title by viewModel.currentDrawingTitle.collectAsState()
                    val layers by viewModel.layers.collectAsState()
                    val activeLayerId by viewModel.activeLayerId.collectAsState()
                    val activeBrushColor by viewModel.activeBrushColor.collectAsState()
                    val activeBrushSize by viewModel.activeBrushSize.collectAsState()
                    val isEraserMode by viewModel.isEraserMode.collectAsState()
                    val currentPaletteIndex by viewModel.currentPaletteIndex.collectAsState()
                    val canUndo by viewModel.canUndo.collectAsState(initial = false)
                    val canRedo by viewModel.canRedo.collectAsState(initial = false)
                    val activeStroke by viewModel.activeStroke.collectAsState()

                    Crossfade(targetState = currentId, label = "ScreenTransition") { activeId ->
                        if (activeId == null) {
                            GalleryScreen(
                                drawings = drawings,
                                onCreateNew = { newTitle ->
                                    viewModel.createNewDrawing(newTitle)
                                },
                                onSelectDrawing = { selectedId ->
                                    viewModel.selectDrawing(selectedId)
                                },
                                onDeleteDrawing = { selectedId ->
                                    viewModel.deleteDrawing(selectedId)
                                }
                            )
                        } else {
                            DrawingScreen(
                                title = title,
                                layers = layers,
                                activeLayerId = activeLayerId,
                                activeStroke = activeStroke,
                                activeBrushColor = activeBrushColor,
                                activeBrushSize = activeBrushSize,
                                isEraserMode = isEraserMode,
                                currentPaletteIndex = currentPaletteIndex,
                                canUndo = canUndo,
                                canRedo = canRedo,
                                onBack = {
                                    viewModel.closeDrawing()
                                },
                                onTitleChange = { newTitle ->
                                    viewModel.updateTitle(newTitle)
                                },
                                onUndo = {
                                    viewModel.undo()
                                },
                                onRedo = {
                                    viewModel.redo()
                                },
                                onBrushColorChange = { col ->
                                    viewModel.setBrushColor(col)
                                },
                                onBrushSizeChange = { sz ->
                                    viewModel.setBrushSize(sz)
                                },
                                onEraserModeChange = { erase ->
                                    viewModel.setEraserMode(erase)
                                },
                                onPaletteIndexChange = { idx ->
                                    viewModel.setPaletteIndex(idx)
                                },
                                onAddLayer = {
                                    viewModel.addLayer()
                                },
                                onDeleteLayer = { id ->
                                    viewModel.deleteLayer(id)
                                },
                                onReorderLayers = { from, to ->
                                    viewModel.reorderLayers(from, to)
                                },
                                onLayerVisibilityChange = { id, vis ->
                                    viewModel.setLayerVisibility(id, vis)
                                },
                                onLayerOpacityChange = { id, op ->
                                    viewModel.setLayerOpacity(id, op)
                                },
                                onSelectActiveLayer = { id ->
                                    viewModel.selectActiveLayer(id)
                                },
                                onClearActiveLayer = {
                                    viewModel.clearActiveLayer()
                                },
                                onDragStart = { x, y ->
                                    viewModel.startNewStroke(x, y)
                                },
                                onDrag = { x, y ->
                                    viewModel.appendPointToStroke(x, y)
                                },
                                onDragEnd = {
                                    viewModel.finishStroke()
                                },
                                onSaveThumbnail = { base64 ->
                                    viewModel.saveCurrentDrawingWithThumbnail(base64)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
