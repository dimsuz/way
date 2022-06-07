package ru.dimsuz.way.sample.android.ui.foundation

import android.util.Log
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<VS : Any> {
  abstract val viewStateFlow: StateFlow<VS>

  open fun onAttach() {
    Log.d(javaClass.simpleName, "onAttach")
  }

  open fun onDetach() {
    Log.d(javaClass.simpleName, "onDetach")
  }

  fun destroy() {
    Log.d(javaClass.simpleName, "destroy")
  }
}
