package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel

class CredentialsViewModel : BaseViewModel<CredentialsViewState>() {
  override val viewStateFlow: StateFlow<CredentialsViewState> = MutableStateFlow(CredentialsViewState())
}
