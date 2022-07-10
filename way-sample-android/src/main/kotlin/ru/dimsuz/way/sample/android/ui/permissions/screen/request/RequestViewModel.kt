package ru.dimsuz.way.sample.android.ui.permissions.screen.request

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.login.FlowEvent

class RequestViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<RequestViewState>() {
  override val viewStateFlow: StateFlow<RequestViewState> = MutableStateFlow(RequestViewState())

  fun onContinue() {
    eventSink.sendEvent(FlowEvent.Continue)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
