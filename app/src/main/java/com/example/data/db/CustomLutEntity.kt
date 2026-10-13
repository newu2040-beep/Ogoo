package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_luts")
data class CustomLutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = "Custom",
    val description: String = "User created preset",
    val recipeJson: String,
    val intensity: Float = 1.0f,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
