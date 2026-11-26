package com.example.foundit.validation

object FormValidation {
    fun required(value: String): String? = if (value.isBlank()) "This field is required" else null
    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !value.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Enter a valid email address"
        else -> null
    }
    fun password(value: String): String? = when {
        value.isBlank() -> "Password is required"
        value.length < 8 -> "Password must contain at least 8 characters"
        else -> null
    }
}
