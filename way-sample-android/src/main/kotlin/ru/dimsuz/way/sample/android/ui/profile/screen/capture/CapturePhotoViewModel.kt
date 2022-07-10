package ru.dimsuz.way.sample.android.ui.profile.screen.capture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.Event
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.login.FlowEvent

class CapturePhotoViewModel(
  private val eventSink: FlowEventSink
) : BaseViewModel<CapturePhotoViewState>() {
  override val viewStateFlow: StateFlow<CapturePhotoViewState> = MutableStateFlow(CapturePhotoViewState())

  fun onContinue() {
    eventSink.sendEvent(FlowEvent.Continue)
  }

  fun onBack() {
    eventSink.sendEvent(Event.BACK)
  }
}
