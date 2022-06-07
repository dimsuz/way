package ru.dimsuz.way.sample.android.ui.foundation

import ru.dimsuz.way.Event

fun interface FlowEventSink {
  fun sendEvent(event: Event)
}
