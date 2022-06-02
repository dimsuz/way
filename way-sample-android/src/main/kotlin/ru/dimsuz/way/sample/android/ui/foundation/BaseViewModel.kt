package ru.dimsuz.way.sample.android.ui.foundation

import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<VS : Any> {
  abstract val viewStateFlow: StateFlow<VS>

  open fun onAttach() {

  }

  open fun onDetach() {

  }

  fun destroy() {

  }
}
