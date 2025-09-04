package com.swadratna.swadratna_staff.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : BaseState, E : BaseEvent> : ViewModel() {
    private val _state = MutableStateFlow(initState())
    val state: StateFlow<S> = _state.asStateFlow()

    abstract fun initState(): S

    abstract fun onEvent(event: E)

    protected fun setState(reduce: S.() -> S) {
        val newState = state.value.reduce()
        _state.value = newState
    }

    protected fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}

interface BaseState

interface BaseEvent