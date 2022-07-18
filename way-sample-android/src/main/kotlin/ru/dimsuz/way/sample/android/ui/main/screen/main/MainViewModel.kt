package ru.dimsuz.way.sample.android.ui.main.screen.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.main.FlowEvent

class MainViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<MainViewState>() {
  override val viewStateFlow: StateFlow<MainViewState> = MutableStateFlow(MainViewState())

  fun onViewProfile() {
    eventSink.sendEvent(FlowEvent.ViewProfile)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
