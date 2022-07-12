package ru.dimsuz.way.sample.android.ui.permissions.screen.request

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.permissions.FlowEvent

class RequestViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<RequestViewState>() {
  override val viewStateFlow: StateFlow<RequestViewState> = MutableStateFlow(RequestViewState())

  fun onContinueGranted() {
    eventSink.sendEvent(FlowEvent.Granted)
  }

  fun onContinueDenied() {
    eventSink.sendEvent(FlowEvent.Denied)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
