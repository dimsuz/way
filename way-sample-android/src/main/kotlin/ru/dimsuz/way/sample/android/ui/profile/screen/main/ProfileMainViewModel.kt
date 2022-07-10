package ru.dimsuz.way.sample.android.ui.profile.screen.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.login.FlowEvent

class ProfileMainViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<ProfileMainViewState>() {
  override val viewStateFlow: StateFlow<ProfileMainViewState> = MutableStateFlow(ProfileMainViewState())

  fun onContinue() {
    eventSink.sendEvent(FlowEvent.Continue)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
