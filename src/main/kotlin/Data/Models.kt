package com.example.Data

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val type: String, val identifier: String, val password: String? = null)

@Serializable
data class AuthResponse(val token: String, val role: String, val guestId: Int? = null)

@Serializable
data class CreateGuestRequest(
    val fullName: String,
    val phone: String,
    val roomNumber: String
)

@Serializable
data class CreateTicketRequest(val categoryId: Int, val description: String)

@Serializable
data class TicketDto(
    val id: Int,
    val guestName: String,
    val roomNumber: String,
    val categoryName: String,
    val description: String,
    val status: String,
    val createdAt: String
)

@Serializable
data class CreateStaffRequest(
    val username: String,
    val password: String,
    val role: String
)

@Serializable
data class GuestResponse(
    val id: Int,
    val fullName: String,
    val phone: String,
    val roomNumber: String,
    val isActive: Boolean
)

@Serializable
data class StaffResponse(
    val id: Int,
    val username: String,
    val role: String
)

@Serializable
data class UpdateGuestRequest(
    val fullName: String,
    val phone: String,
    val roomNumber: String,
    val isActive: Boolean
)

@Serializable
data class UpdateStaffRequest(
    val username: String,
    val role: String
)

@Serializable
data class UpdateStatusRequest(val status: String, val assignedStaffId: Int? = null)