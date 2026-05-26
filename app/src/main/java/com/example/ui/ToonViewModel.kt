package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ToonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DrawingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DrawingRepository(database.drawingDao())
    }

    val allDrawings: StateFlow<List<DrawingEntity>> = repository.allDrawings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Drawing States
    private val _currentDrawingId = MutableStateFlow<Int?>(null)
    val currentDrawingId: StateFlow<Int?> = _currentDrawingId.asStateFlow()

    private val _currentDrawingTitle = MutableStateFlow("My Masterpiece")
    val currentDrawingTitle: StateFlow<String> = _currentDrawingTitle.asStateFlow()

    private val _layers = MutableStateFlow<List<ToonLayer>>(emptyList())
    val layers: StateFlow<List<ToonLayer>> = _layers.asStateFlow()

    private val _activeLayerId = MutableStateFlow("")
    val activeLayerId: StateFlow<String> = _activeLayerId.asStateFlow()

    // Brush Settings
    private val _activeBrushColor = MutableStateFlow(0xFF000000.toInt()) // Default Black
    val activeBrushColor: StateFlow<Int> = _activeBrushColor.asStateFlow()

    private val _activeBrushSize = MutableStateFlow(8f)
    val activeBrushSize: StateFlow<Float> = _activeBrushSize.asStateFlow()

    private val _isEraserMode = MutableStateFlow(false)
    val isEraserMode: StateFlow<Boolean> = _isEraserMode.asStateFlow()

    // Selected palette category indices (0: Saturday Cartoon, 1: Retro Pop-art, 2: Manga Noir)
    private val _currentPaletteIndex = MutableStateFlow(0)
    val currentPaletteIndex: StateFlow<Int> = _currentPaletteIndex.asStateFlow()

    // Undo/Redo Stacks
    private val _undoStack = MutableStateFlow<List<List<ToonLayer>>>(emptyList())
    private val _redoStack = MutableStateFlow<List<List<ToonLayer>>>(emptyList())

    val canUndo: Flow<Boolean> = _undoStack.map { it.isNotEmpty() }
    val canRedo: Flow<Boolean> = _redoStack.map { it.isNotEmpty() }

    // Active drawing touch stroke (uncommitted)
    private val _activeStroke = MutableStateFlow<ToonStroke?>(null)
    val activeStroke: StateFlow<ToonStroke?> = _activeStroke.asStateFlow()

    // Create a new empty cartoon drawing
    fun createNewDrawing(title: String) {
        viewModelScope.launch {
            val initialLayers = listOf(
                ToonLayer(
                    id = UUID.randomUUID().toString(),
                    name = "Manga Colors",
                    strokes = emptyList()
                ),
                ToonLayer(
                    id = UUID.randomUUID().toString(),
                    name = "Ink Outline",
                    strokes = emptyList()
                ),
                ToonLayer(
                    id = UUID.randomUUID().toString(),
                    name = "Draft Sketch",
                    strokes = emptyList()
                )
            )
            // Initial selected layer is the Ink Outline layer
            val outlineLayerId = initialLayers[1].id
            
            val entity = DrawingEntity(
                title = title,
                layers = initialLayers,
                previewBase64 = null
            )
            val newId = repository.saveDrawing(entity)
            _currentDrawingId.value = newId.toInt()
            _currentDrawingTitle.value = title
            _layers.value = initialLayers
            _activeLayerId.value = outlineLayerId
            clearHistory()
        }
    }

    // Select a drawing to edit
    fun selectDrawing(id: Int) {
        viewModelScope.launch {
            val drawing = repository.getDrawingById(id)
            if (drawing != null) {
                _currentDrawingId.value = drawing.id
                _currentDrawingTitle.value = drawing.title
                _layers.value = drawing.layers
                _activeLayerId.value = drawing.layers.firstOrNull()?.id ?: ""
                clearHistory()
            }
        }
    }

    // Return to Gallery
    fun closeDrawing() {
        saveCurrentDrawing()
        _currentDrawingId.value = null
        _activeLayerId.value = ""
        _layers.value = emptyList()
        clearHistory()
    }

    // Delete a drawing
    fun deleteDrawing(id: Int) {
        viewModelScope.launch {
            repository.deleteDrawingById(id)
            if (_currentDrawingId.value == id) {
                _currentDrawingId.value = null
            }
        }
    }

    // Brush controls
    fun setBrushColor(color: Int) {
        _activeBrushColor.value = color
    }

    fun setBrushSize(size: Float) {
        _activeBrushSize.value = size
    }

    fun setEraserMode(enabled: Boolean) {
        _isEraserMode.value = enabled
    }

    fun setPaletteIndex(index: Int) {
        _currentPaletteIndex.value = index
    }

    // Layer Management
    fun addLayer() {
        pushToUndo()
        val current = _layers.value.toMutableList()
        val newLayer = ToonLayer(
            id = UUID.randomUUID().toString(),
            name = "Layer ${current.size + 1}",
            strokes = emptyList()
        )
        // Add new layer on top
        current.add(0, newLayer)
        _layers.value = current
        _activeLayerId.value = newLayer.id
    }

    fun deleteLayer(layerId: String) {
        if (_layers.value.size <= 1) return // Need at least 1 layer
        pushToUndo()
        val current = _layers.value.filter { it.id != layerId }
        _layers.value = current
        if (_activeLayerId.value == layerId) {
            _activeLayerId.value = current.firstOrNull()?.id ?: ""
        }
    }

    fun reorderLayers(fromIndex: Int, toIndex: Int) {
        if (fromIndex in _layers.value.indices && toIndex in _layers.value.indices && fromIndex != toIndex) {
            pushToUndo()
            val current = _layers.value.toMutableList()
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _layers.value = current
        }
    }

    fun setLayerVisibility(layerId: String, visible: Boolean) {
        val current = _layers.value.map {
            if (it.id == layerId) it.copy(visible = visible) else it
        }
        _layers.value = current
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        val current = _layers.value.map {
            if (it.id == layerId) it.copy(opacity = opacity) else it
        }
        _layers.value = current
    }

    fun selectActiveLayer(layerId: String) {
        _activeLayerId.value = layerId
    }

    fun updateTitle(newTitle: String) {
        _currentDrawingTitle.value = newTitle
        saveCurrentDrawing()
    }

    // Real-time Stroke Drawing Operations
    fun startNewStroke(x: Float, y: Float) {
        pushToUndo()
        _redoStack.value = emptyList() // Clear redo on action
        _activeStroke.value = ToonStroke(
            points = listOf(ToonPoint(x, y)),
            color = _activeBrushColor.value,
            size = _activeBrushSize.value,
            isEraser = _isEraserMode.value
        )
    }

    fun appendPointToStroke(x: Float, y: Float) {
        val currentStroke = _activeStroke.value ?: return
        val currentPoints = currentStroke.points.toMutableList()
        currentPoints.add(ToonPoint(x, y))
        _activeStroke.value = currentStroke.copy(points = currentPoints)
    }

    fun finishStroke() {
        val completedStroke = _activeStroke.value
        _activeStroke.value = null
        if (completedStroke != null && completedStroke.points.size >= 1) {
            val layerId = _activeLayerId.value
            val currentLayers = _layers.value.map { layer ->
                if (layer.id == layerId) {
                    layer.copy(strokes = layer.strokes + completedStroke)
                } else {
                    layer
                }
            }
            _layers.value = currentLayers
            saveCurrentDrawing()
        }
    }

    fun clearActiveLayer() {
        pushToUndo()
        val layerId = _activeLayerId.value
        _layers.value = _layers.value.map { layer ->
            if (layer.id == layerId) {
                layer.copy(strokes = emptyList())
            } else {
                layer
            }
        }
        saveCurrentDrawing()
    }

    // History undo/redo mechanics
    private fun pushToUndo() {
        val currentLayers = _layers.value
        val undoList = _undoStack.value.toMutableList()
        undoList.add(currentLayers)
        _undoStack.value = undoList
    }

    fun undo() {
        val undoList = _undoStack.value.toMutableList()
        if (undoList.isNotEmpty()) {
            val lastState = undoList.removeAt(undoList.size - 1)
            
            // Push active state to redo stack
            val redoList = _redoStack.value.toMutableList()
            redoList.add(_layers.value)
            _redoStack.value = redoList

            _layers.value = lastState
            _undoStack.value = undoList
            
            saveCurrentDrawing()
        }
    }

    fun redo() {
        val redoList = _redoStack.value.toMutableList()
        if (redoList.isNotEmpty()) {
            val nextState = redoList.removeAt(redoList.size - 1)

            // Push active state to undo stack
            val undoList = _undoStack.value.toMutableList()
            undoList.add(_layers.value)
            _undoStack.value = undoList

            _layers.value = nextState
            _redoStack.value = redoList

            saveCurrentDrawing()
        }
    }

    private fun clearHistory() {
        _undoStack.value = emptyList()
        _redoStack.value = emptyList()
    }

    fun saveCurrentDrawingWithThumbnail(previewBase64: String?) {
        val drawingId = _currentDrawingId.value ?: return
        viewModelScope.launch {
            val drawing = DrawingEntity(
                id = drawingId,
                title = _currentDrawingTitle.value,
                layers = _layers.value,
                previewBase64 = previewBase64,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDrawing(drawing)
        }
    }

    private fun saveCurrentDrawing() {
        val drawingId = _currentDrawingId.value ?: return
        viewModelScope.launch {
            val drawing = DrawingEntity(
                id = drawingId,
                title = _currentDrawingTitle.value,
                layers = _layers.value,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDrawing(drawing)
        }
    }
}
