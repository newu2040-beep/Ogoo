package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val sourceUri: String,
    val thumbnailUri: String? = null,
    val sampleResName: String? = null,
    val recipeJson: String,
    val isDraft: Boolean = true,
    val isFavorite: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
