package com.kam666.mealplanner.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StringListConverter {
    @TypeConverter
    fun fromJson(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun toJson(list: List<String>): String = Json.encodeToString(list)
}
