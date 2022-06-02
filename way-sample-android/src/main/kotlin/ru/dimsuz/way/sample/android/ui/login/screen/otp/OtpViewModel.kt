package ru.dimsuz.way.sample.android.ui.login.screen.otp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel

class OtpViewModel : BaseViewModel<OtpViewState>() {
  override val viewStateFlow: StateFlow<OtpViewState> = MutableStateFlow(OtpViewState())
}
