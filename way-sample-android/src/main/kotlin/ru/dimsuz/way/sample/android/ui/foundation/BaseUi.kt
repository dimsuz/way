package ru.dimsuz.way.sample.android.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun <VS : Any> BaseUi(
  viewModel: BaseViewModel<VS>,
  content: @Composable (VS) -> Unit,
) {
  val state by viewModel.viewStateFlow.collectAsState()
  content(state)
}
