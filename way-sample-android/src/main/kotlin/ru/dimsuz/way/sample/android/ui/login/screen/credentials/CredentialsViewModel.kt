package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.login.FlowEvent

class CredentialsViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<CredentialsViewState>() {
  override val viewStateFlow: StateFlow<CredentialsViewState> = MutableStateFlow(CredentialsViewState())

  fun onContinue() {
    eventSink.sendEvent(FlowEvent.Continue)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
