package ru.dimsuz.way.sample.android.flow.event

import ru.dimsuz.way.Event

/**
 * Events potentially affecting global app state, they usually lead to drastic changes in
 * navigation graph and are broadcast "bottom-up"
 */
object AppStateEvent {
  val PermissionsDeniedPermanently = Event(Event.Name("all_permissions_denied_permanently"))
  val AuthTokensExpired = Event(Event.Name("auth_tokens_expired"))
}
