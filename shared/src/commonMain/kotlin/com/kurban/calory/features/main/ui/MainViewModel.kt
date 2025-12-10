package com.kurban.calory.features.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurban.calory.core.domain.AppDispatchers
import com.kurban.calory.core.mvi.Store
import com.kurban.calory.core.time.DayProvider
import com.kurban.calory.features.main.domain.AddTrackedFoodUseCase
import com.kurban.calory.features.main.domain.DeleteConsumedFoodUseCase
import com.kurban.calory.features.main.domain.GetTrackedForDayUseCase
import com.kurban.calory.features.main.domain.SearchFoodUseCase
import com.kurban.calory.features.main.ui.logic.AddFoodMiddleware
import com.kurban.calory.features.main.ui.logic.LoadDayMiddleware
import com.kurban.calory.features.main.ui.logic.RemoveEntryMiddleware
import com.kurban.calory.features.main.ui.logic.SearchMiddleware
import com.kurban.calory.features.main.ui.logic.mainReducer
import com.kurban.calory.features.main.ui.model.MainAction
import com.kurban.calory.features.main.ui.model.MainEffect
import com.kurban.calory.features.main.ui.model.MainIntent
import com.kurban.calory.features.main.ui.model.MainUiState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    searchFood: SearchFoodUseCase,
    getTrackedForDay: GetTrackedForDayUseCase,
    deleteTrackedFood: DeleteConsumedFoodUseCase,
    addTrackedFoodUseCase: AddTrackedFoodUseCase,
    dispatchers: AppDispatchers,
    private val dayProvider: DayProvider,
) : ViewModel() {

    private val store = Store(
        initialState = MainUiState(),
        reducer = mainReducer(),
        middlewares = listOf(
            SearchMiddleware(searchFood, dispatchers, viewModelScope),
            LoadDayMiddleware(getTrackedForDay, dispatchers, dayProvider),
            AddFoodMiddleware(addTrackedFoodUseCase),
            RemoveEntryMiddleware(deleteTrackedFood, dayProvider)
        ),
        scope = viewModelScope,
        initialActions = listOf(MainAction.LoadDay(dayProvider.currentDayId()))
    )

    val uiState: StateFlow<MainUiState> = store.state
    val effects: SharedFlow<MainEffect> = store.effects

    fun dispatch(intent: MainIntent) {
        when (intent) {
            MainIntent.LoadToday -> store.dispatch(MainAction.LoadDay(dayProvider.currentDayId()))
            is MainIntent.QueryChanged -> store.dispatch(MainAction.QueryChanged(intent.query))
            is MainIntent.FoodSelected -> store.dispatch(MainAction.FoodSelected(intent.food))
            is MainIntent.GramsChanged -> store.dispatch(MainAction.GramsChanged(intent.gramsInput))
            MainIntent.AddSelectedFood -> store.dispatch(MainAction.AddSelectedFood)
            is MainIntent.RemoveEntry -> store.dispatch(MainAction.RemoveEntry(intent.entryId))
            MainIntent.ClearError -> store.dispatch(MainAction.ClearError)
        }
    }
}
