package com.kurban.calory.features.main.ui.model

sealed class MainIntent {
    object LoadToday : MainIntent()
    data class QueryChanged(val query: String) : MainIntent()
    data class FoodSelected(val food: com.kurban.calory.features.main.domain.model.Food) : MainIntent()
    data class GramsChanged(val gramsInput: String) : MainIntent()
    object AddSelectedFood : MainIntent()
    data class RemoveEntry(val entryId: Long) : MainIntent()
    object ClearError : MainIntent()
}
