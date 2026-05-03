package com.dec.attendpro.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val rollNumber: String? = null,
    val semester: String? = null,
    val branch: String? = null,
    val phoneNumber: String? = null,
    val idImage: String? = null
)
