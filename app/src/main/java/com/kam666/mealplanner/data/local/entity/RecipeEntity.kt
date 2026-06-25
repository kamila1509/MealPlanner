package com.kam666.mealplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val servings: Int,
    val imageUri: String? = null,
    val preparationTimeMinutes: Int? = null,
    val preparationSteps: String = "[]"
)
