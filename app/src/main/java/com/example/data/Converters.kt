package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ToonLayer::class.java)
    private val adapter = moshi.adapter<List<ToonLayer>>(listType)

    @TypeConverter
    fun fromLayersJson(json: String): List<ToonLayer> {
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toLayersJson(layers: List<ToonLayer>): String {
        return adapter.toJson(layers)
    }
}
