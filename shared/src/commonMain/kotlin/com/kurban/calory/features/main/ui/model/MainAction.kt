package com.kurban.calory.features.main.ui.model

import com.kurban.calory.features.main.domain.model.Food

sealed class MainAction {
    data class QueryChanged(val query: String) : MainAction()
    data class FoodSelected(val food: Food) : MainAction()
    data class GramsChanged(val gramsInput: String) : MainAction()
    data class LoadDay(val dayId: String) : MainAction()
    object AddSelectedFood : MainAction()
    data class RemoveEntry(val entryId: Long) : MainAction()

    data class SearchSuccess(val results: List<Food>) : MainAction()
    data class SearchFailure(val message: String) : MainAction()

    data class LoadDaySuccess(
        val items: List<UITrackedFood>,
        val totals: MacroTotals
    ) : MainAction()

    data class LoadDayFailure(val message: String) : MainAction()
    data class AddFoodFailure(val message: String) : MainAction()
    data class RemoveEntryFailure(val message: String) : MainAction()
    object ClearError : MainAction()
}

data class MacroTotals(
    val calories: Double = 0.0,
    val proteins: Double = 0.0,
    val fats: Double = 0.0,
    val carbs: Double = 0.0
)
