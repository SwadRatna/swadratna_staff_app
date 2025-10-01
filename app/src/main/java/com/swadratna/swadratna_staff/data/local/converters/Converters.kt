package com.swadratna.swadratna_staff.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swadratna.swadratna_staff.data.remote.model.Address // Assuming Address is in remote.model
import com.swadratna.swadratna_staff.data.remote.model.Location // Import remote Location model

class Converters {

    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return Gson().toJson(location)
    }

    @TypeConverter
    fun toLocation(locationString: String?): Location? {
        if (locationString == null) return null
        val type = object : TypeToken<Location>() {}.type
        return Gson().fromJson(locationString, type)
    }

    @TypeConverter
    fun fromAddress(address: Address?): String? {
        return Gson().toJson(address)
    }

    @TypeConverter
    fun toAddress(addressString: String?): Address? {
        if (addressString == null) return null
        val type = object : TypeToken<Address>() {}.type
        return Gson().fromJson(addressString, type)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        return string?.split(",")?.map { it.trim() }
    }
}
