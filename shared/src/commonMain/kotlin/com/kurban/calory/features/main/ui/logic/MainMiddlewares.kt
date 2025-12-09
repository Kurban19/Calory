package com.kurban.calory.features.main.ui.logic

import com.kurban.calory.core.domain.AppDispatchers
import com.kurban.calory.core.mvi.Middleware
import com.kurban.calory.core.time.DayProvider
import com.kurban.calory.features.main.domain.AddTrackedFoodUseCase
import com.kurban.calory.features.main.domain.DeleteConsumedFoodUseCase
import com.kurban.calory.features.main.domain.GetTrackedForDayUseCase
import com.kurban.calory.features.main.domain.SearchFoodUseCase
import com.kurban.calory.features.main.domain.model.TrackedFood
import com.kurban.calory.features.main.ui.model.MainAction
import com.kurban.calory.features.main.ui.model.MainEffect
import com.kurban.calory.features.main.ui.model.MainUiState
import com.kurban.calory.features.main.ui.model.UITrackedFood
import com.kurban.calory.features.main.ui.model.MacroTotals
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainMiddlewares(
    private val searchFood: SearchFoodUseCase,
    private val getTrackedForDay: GetTrackedForDayUseCase,
    private val deleteTrackedFood: DeleteConsumedFoodUseCase,
    private val addTrackedFoodUseCase: AddTrackedFoodUseCase,
    private val dispatchers: AppDispatchers,
    private val dayProvider: DayProvider,
) {

    fun build(): List<Middleware<MainUiState, MainAction, MainEffect>> = listOf(coreMiddleware())

    private fun coreMiddleware(): Middleware<MainUiState, MainAction, MainEffect> = { action, state, dispatch, emitEffect ->
        when (action) {
            is MainAction.QueryChanged -> handleSearch(action.query, state, dispatch, emitEffect)
            is MainAction.LoadDay -> handleLoadDay(action.dayId, dispatch, emitEffect)
            is MainAction.AddSelectedFood -> handleAddFood(state, dispatch, emitEffect)
            is MainAction.RemoveEntry -> handleRemoveEntry(action.entryId, dispatch, emitEffect)
            else -> Unit
        }
    }

    private suspend fun handleSearch(
        query: String,
        state: MainUiState,
        dispatch: suspend (MainAction) -> Unit,
        emitEffect: suspend (MainEffect) -> Unit
    ) {
        if (query.isBlank()) {
            dispatch(MainAction.SearchSuccess(emptyList()))
            return
        }
        try {
            val result = searchFood(SearchFoodUseCase.Parameters(query)) ?: emptyList()
            dispatch(MainAction.SearchSuccess(result))
        } catch (e: Exception) {
            emitEffect(MainEffect.Error(e.message ?: "Ошибка поиска"))
            dispatch(MainAction.SearchFailure(e.message.orEmpty()))
        }
    }

    private suspend fun handleLoadDay(
        dayId: String,
        dispatch: suspend (MainAction) -> Unit,
        emitEffect: suspend (MainEffect) -> Unit
    ) {
        try {
            val tracked = withContext(dispatchers.io) {
                getTrackedForDay(GetTrackedForDayUseCase.Parameters(dayId)) ?: emptyList()
            }
            val uiItems = tracked.map { it.toUi() }
            val totals = calculateTotals(uiItems)
            dispatch(MainAction.LoadDaySuccess(uiItems, totals))
        } catch (e: Exception) {
            emitEffect(MainEffect.Error(e.message ?: "Не удалось загрузить данные"))
            dispatch(MainAction.LoadDayFailure(e.message.orEmpty()))
        }
    }

    private suspend fun handleAddFood(
        state: MainUiState,
        dispatch: suspend (MainAction) -> Unit,
        emitEffect: suspend (MainEffect) -> Unit
    ) {
        val selected = state.selectedFood ?: run {
            emitEffect(MainEffect.Error("Сначала выберите продукт"))
            return
        }
        val gramsValue = state.gramsInput.replace(',', '.').toDoubleOrNull()?.roundToInt() ?: run {
            emitEffect(MainEffect.Error("Введите размер порции в граммах"))
            return
        }

        try {
            when (val result = addTrackedFoodUseCase(AddTrackedFoodUseCase.Parameters(selected.name, gramsValue))) {
                is AddTrackedFoodUseCase.Result.Success -> dispatch(MainAction.LoadDay(result.dayId))
                is AddTrackedFoodUseCase.Result.Error -> emitEffect(MainEffect.Error(result.message))
                null -> emitEffect(MainEffect.Error("Не удалось добавить продукт"))
            }
        } catch (e: Exception) {
            emitEffect(MainEffect.Error(e.message ?: "Не удалось добавить продукт"))
            dispatch(MainAction.AddFoodFailure(e.message.orEmpty()))
        }
    }

    private suspend fun handleRemoveEntry(
        entryId: Long,
        dispatch: suspend (MainAction) -> Unit,
        emitEffect: suspend (MainEffect) -> Unit
    ) {
        try {
            deleteTrackedFood(DeleteConsumedFoodUseCase.Parameters(entryId))
            dispatch(MainAction.LoadDay(dayProvider.currentDayId()))
        } catch (e: Exception) {
            emitEffect(MainEffect.Error(e.message ?: "Не удалось удалить запись"))
            dispatch(MainAction.RemoveEntryFailure(e.message.orEmpty()))
        }
    }

    private fun calculateTotals(consumed: List<UITrackedFood>): MacroTotals {
        return consumed.fold(MacroTotals()) { acc, food ->
            MacroTotals(
                calories = acc.calories + food.calories,
                proteins = acc.proteins + food.proteins,
                fats = acc.fats + food.fats,
                carbs = acc.carbs + food.carbs
            )
        }
    }

    private fun TrackedFood.toUi(): UITrackedFood = UITrackedFood(
        entryId = id,
        foodId = foodId,
        name = name,
        grams = grams,
        calories = calories,
        proteins = proteins,
        fats = fats,
        carbs = carbs
    )
}
