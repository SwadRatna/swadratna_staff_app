package com.swadratna.swadratna_staff.ui.screens.home

import com.swadratna.swadratna_staff.base.BaseEvent
import com.swadratna.swadratna_staff.base.BaseState
import com.swadratna.swadratna_staff.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<HomeState, HomeEvent>() {
    override fun initState(): HomeState = HomeState()

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.UpdateMessage -> setState { copy(message = event.message) }
        }
    }
}

data class HomeState(
    val message: String = "Welcome to SwadRatna Staff App"
) : BaseState

sealed class HomeEvent : BaseEvent {
    data class UpdateMessage(val message: String) : HomeEvent()
}