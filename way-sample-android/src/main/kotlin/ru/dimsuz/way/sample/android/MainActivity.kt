package ru.dimsuz.way.sample.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.dimsuz.way.BackStack
import ru.dimsuz.way.CommandBuilder
import ru.dimsuz.way.Event
import ru.dimsuz.way.NavigationMachine
import ru.dimsuz.way.NavigationService
import ru.dimsuz.way.Path
import ru.dimsuz.way.sample.android.flow.app.AppFlow
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.ui.foundation.Screen

class MainActivity : ComponentActivity() {
  private val scope = CoroutineScope(Dispatchers.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val eventSink = MutableSharedFlow<Event>()
    val commandBuilder = ComposableCommandBuilder()

    val navigationService = NavigationService(
      machine = NavigationMachine(AppFlow.buildNode(eventSink = {  scope.launch { eventSink.emit(it) } })),
      commandBuilder = commandBuilder,
      onCommand = {
        it.invoke()
      }
    )

    eventSink
      .onEach { navigationService.sendEvent(it) }
      .launchIn(scope)

    navigationService.start()

    setContent {
      MaterialTheme {
        commandBuilder.currentScreen?.Content()
        LaunchedEffect(commandBuilder.currentScreen) {
          commandBuilder.currentScreen?.onAttach()
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}

private class ComposableCommandBuilder : CommandBuilder<() -> Unit> {
  var currentScreen: Screen? by mutableStateOf(null)
  var previousScreen: Screen? by mutableStateOf(null)
  private val activeScreens: MutableMap<Path, Screen> = mutableMapOf()

  override fun invoke(oldBackStack: BackStack, newBackStack: BackStack, newState: Any): () -> Unit {
    return {
      require(newState is FlowState) {
        "expected navigation state to be instance of ${FlowState::class.simpleName}, " +
          "but got an instance of ${newState::class.simpleName}"
      }
      println(buildString {
        appendLine("building command")
        append("  old backstack: $oldBackStack")
        append("  new backstack: $newBackStack")
        appendLine()
        append("  new state: $newState")
      })
      if (newBackStack.isNotEmpty()) {
        currentScreen = getOrCreateActiveScreen(newBackStack.last(), newState)
          ?: error("don't know how to make screen for a backstack: $newBackStack")
        if (previousScreen != currentScreen) {
          previousScreen?.onDetach()
          previousScreen = currentScreen
        }
        cleanUpActiveScreens(newBackStack)
      }
      Log.d("builder", "curr screen $currentScreen")
    }
  }

  private fun getOrCreateActiveScreen(path: Path, flowState: FlowState): Screen? {
    return activeScreens[path]
      ?: flowState.screens[path.lastSegment]?.also { activeScreens[path] = it }
  }

  private fun cleanUpActiveScreens(backStack: BackStack) {
    activeScreens.forEach { (path, screen) ->
      if (path !in backStack) {
        screen.destroy()
      }
    }
  }
}
