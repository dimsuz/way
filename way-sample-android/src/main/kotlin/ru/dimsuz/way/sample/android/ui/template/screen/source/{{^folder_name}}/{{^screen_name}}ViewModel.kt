package ru.dimsuz.way.sample.android.ui.{{^feature_name}}.screen.{{^folder_name}}

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.login.FlowEvent

class {{^screen_name}}ViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<{{^screen_name}}ViewState>() {
  override val viewStateFlow: StateFlow<{{^screen_name}}ViewState> = MutableStateFlow({{^screen_name}}ViewState())

  fun onContinue() {
    eventSink.sendEvent(FlowEvent.Continue)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
