package com.kurban.calory.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

typealias Reducer<S, A> = (S, A) -> S

typealias Middleware<S, A, E> = suspend (
    action: A,
    state: S,
    dispatch: suspend (A) -> Unit,
    emitEffect: suspend (E) -> Unit
) -> Unit

class Store<S, A, E>(
    initialState: S,
    private val reducer: Reducer<S, A>,
    private val middlewares: List<Middleware<S, A, E>>,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<E>()
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    fun dispatch(action: A) {
        scope.launch {
            middlewares.forEach { middleware ->
                middleware(action, _state.value, { dispatch(it) }, { emitEffect(it) })
            }
            _state.update { reducer(it, action) }
        }
    }

    private suspend fun emitEffect(effect: E) {
        _effects.emit(effect)
    }
}
