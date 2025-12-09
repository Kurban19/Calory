package com.kurban.calory.features.main.ui.model

sealed class MainEffect {
    data class Error(val message: String) : MainEffect()
}
