package com.example.bluetooth.model.database


import androidx.room.TypeConverter
import com.example.bluetooth.model.data.ElevationReading
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromElevationReadingList(value: List<ElevationReading>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toElevationReadingList(value: String): List<ElevationReading> {
        val type = object : TypeToken<List<ElevationReading>>() {}.type
        return gson.fromJson(value, type)
    }
}