package ru.dimsuz.way.sample.android.ui.login

import ru.dimsuz.way.Event

object FlowEvent {
  val Continue = Event(Event.Name("Continue"))
  val OtpSuccess = Event(Event.Name("OtpSuccess"))
  val OtpError = Event(Event.Name("OtpError"))
}
