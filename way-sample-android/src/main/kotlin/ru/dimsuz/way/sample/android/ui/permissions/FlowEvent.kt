package ru.dimsuz.way.sample.android.ui.permissions

import ru.dimsuz.way.Event

object FlowEvent {
  val Granted = Event(Event.Name("permissions_granted"))
  val Denied = Event(Event.Name("permissions_denied"))
}
