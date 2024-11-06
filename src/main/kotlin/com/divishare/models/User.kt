package com.divishare.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val username: String,
    val email: String,
    val passwordHash: String,
    val phonePrefix: String? = null,
    val phoneNumber: String? = null
)
