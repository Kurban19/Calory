package com.kurban.calory.features.main.domain.model

data class Food(
    val id: Long,
    val grams: Int,
    val name: String,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
)