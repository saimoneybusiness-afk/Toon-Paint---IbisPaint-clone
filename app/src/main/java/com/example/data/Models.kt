package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ToonPoint(
    val x: Float,
    val y: Float
)

@JsonClass(generateAdapter = true)
data class ToonStroke(
    val points: List<ToonPoint>,
    val color: Int, // ARGB Int
    val size: Float,
    val isEraser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ToonLayer(
    val id: String,
    val name: String,
    val visible: Boolean = true,
    val opacity: Float = 1.0f, // 0.0 to 1.0
    val strokes: List<ToonStroke> = emptyList()
)
