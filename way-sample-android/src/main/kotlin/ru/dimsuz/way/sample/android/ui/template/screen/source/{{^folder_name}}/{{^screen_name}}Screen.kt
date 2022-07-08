package ru.dimsuz.way.sample.android.ui.{{^feature_name}}.screen.{{^folder_name}}

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class {{^screen_name}}Screen(viewModel: {{^screen_name}}ViewModel) : BaseScreen<{{^screen_name}}ViewState, {{^screen_name}}ViewModel>(viewModel) {
  companion object {
    val key = "{{^folder_name}}"
  }

  @Composable
  override fun Content(viewModel: {{^screen_name}}ViewModel) {
    {{^screen_name}}Ui(viewModel)
  }
}
