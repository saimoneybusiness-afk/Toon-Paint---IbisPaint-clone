package com.example.data

import kotlinx.coroutines.flow.Flow

class DrawingRepository(private val drawingDao: DrawingDao) {
    val allDrawings: Flow<List<DrawingEntity>> = drawingDao.getAllDrawings()

    suspend fun getDrawingById(id: Int): DrawingEntity? {
        return drawingDao.getDrawingById(id)
    }

    suspend fun saveDrawing(drawing: DrawingEntity): Long {
        return drawingDao.insertDrawing(drawing)
    }

    suspend fun deleteDrawingById(id: Int) {
        drawingDao.deleteDrawingById(id)
    }
}
