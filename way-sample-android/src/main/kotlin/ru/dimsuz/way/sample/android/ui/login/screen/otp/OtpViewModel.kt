package ru.dimsuz.way.sample.android.ui.login.screen.otp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.login.FlowEvent

class OtpViewModel(
  private val eventSink: FlowEventSink,
) : BaseViewModel<OtpViewState>() {
  override val viewStateFlow: StateFlow<OtpViewState> = MutableStateFlow(OtpViewState())

  fun onContinue() {
    eventSink.sendEvent(FlowEvent.Continue)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
