package com.dec.attendpro.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromFloatArray(value: FloatArray): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        val type = object : TypeToken<FloatArray>() {}.type
        return Gson().fromJson(value, type)
    }
}
