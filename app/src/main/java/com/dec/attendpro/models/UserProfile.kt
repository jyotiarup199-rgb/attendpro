package com.dec.attendpro.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("id") val id: String,
    @SerialName("full_name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("role") val role: String,
    @SerialName("roll_number") val rollNumber: String? = null,
    @SerialName("semester") val semester: String? = null,
    @SerialName("branch") val branch: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("id_image") val idImage: String? = null
)
