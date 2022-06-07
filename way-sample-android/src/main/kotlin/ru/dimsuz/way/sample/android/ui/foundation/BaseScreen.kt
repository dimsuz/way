package ru.dimsuz.way.sample.android.ui.foundation

import androidx.compose.runtime.Composable

abstract class BaseScreen<VS : Any, VM : BaseViewModel<VS>>(private val viewModel: VM) : Screen {

  @Composable
  final override fun Content() {
    Content(viewModel)
  }

  @Composable
  abstract fun Content(viewModel: VM)

  override fun onAttach() {
    viewModel.onAttach()
  }

  override fun onDetach() {
    viewModel.onDetach()
  }

  override fun destroy() {
    viewModel.destroy()
  }
}
