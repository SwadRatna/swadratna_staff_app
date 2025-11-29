package com.swadratna.swadratna_staff.data.remote.repositories

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// Define the data structure for the error response body: {"error":"Invalid credentials"}
data class ErrorResponse(
    @SerializedName("error") val errorMessage: String
)

// You must have access to the Gson instance used by Retrofit
