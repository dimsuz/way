package ru.dimsuz.way.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import ru.dimsuz.way.BackStack
import ru.dimsuz.way.CommandBuilder
import ru.dimsuz.way.NavigationMachine
import ru.dimsuz.way.NavigationService
import ru.dimsuz.way.sample.android.flow.app.AppFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val navigationService = NavigationService(
      machine = NavigationMachine(AppFlow.buildNode()),
      commandBuilder = ComposableCommandBuilder(),
      onCommand = {
        it.invoke()
      }
    )

    navigationService.start()

    setContent {
      MaterialTheme {
        Surface(color = MaterialTheme.colors.background) {
          Text("Hello world")
        }
      }
    }
  }
}

private class ComposableCommandBuilder : CommandBuilder<() -> Unit> {
  override fun invoke(oldBackStack: BackStack, newBackStack: BackStack): () -> Unit {
    return {
      println(buildString {
        appendLine("building command")
        append("  old backstack: $oldBackStack")
        append("  new backstack: $newBackStack")
      })
    }
  }
}
