package com.kurban.calory.features.main.ui.logic

import com.kurban.calory.core.mvi.Reducer
import com.kurban.calory.features.main.ui.model.MainAction
import com.kurban.calory.features.main.ui.model.MainUiState

fun mainReducer(): Reducer<MainUiState, MainAction> = { state, action ->
    when (action) {
        is MainAction.QueryChanged -> state.copy(query = action.query, isSearching = true)
        is MainAction.GramsChanged -> state.copy(
            gramsInput = action.gramsInput.filter { char -> char.isDigit() || char == '.' || char == ',' }
        )
        is MainAction.FoodSelected -> state.copy(selectedFood = action.food)
        is MainAction.SearchSuccess -> state.copy(
            searchResults = action.results,
            isSearching = false,
            selectedFood = if (action.results.isNotEmpty() && state.selectedFood == null) action.results.first() else state.selectedFood
        )
        is MainAction.SearchFailure -> state.copy(isSearching = false)
        is MainAction.LoadDaySuccess -> state.copy(
            tracked = action.items,
            totalCalories = action.totals.calories,
            totalProteins = action.totals.proteins,
            totalFats = action.totals.fats,
            totalCarbs = action.totals.carbs
        )
        else -> state
    }
}
