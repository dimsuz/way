package ru.dimsuz.way.sample.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen

class MainActivity : ComponentActivity() {
  private val scope = CoroutineScope(Dispatchers.Main)

  @OptIn(ExperimentalAnimationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val eventFlow = MutableSharedFlow<Event>()
    val eventSink: (Event) -> Unit = { event: Event -> scope.launch { eventFlow.emit(event) } }
    val commandBuilder = ComposableCommandBuilder(eventSink)

    val navigationService = NavigationService(
      machine = NavigationMachine(AppFlow.buildNode()),
      commandBuilder = commandBuilder,
      onCommand = {
        it.invoke()
      }
    )

    eventFlow
      .onEach { navigationService.sendEvent(it) }
      .launchIn(scope)

    navigationService.start()

    setContent {
      MaterialTheme {
        AnimatedContent(targetState = commandBuilder.currentScreen, transitionSpec = {
          fadeIn() + slideIntoContainer(towards = AnimatedContentScope.SlideDirection.Start) with fadeOut() + slideOutOfContainer(
            AnimatedContentScope.SlideDirection.Start)
        }) { currentScreen ->
          currentScreen?.Content()
          LaunchedEffect(currentScreen) {
            currentScreen?.onAttach()
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}

private class ComposableCommandBuilder(
  private val flowEventSink: FlowEventSink,
) : CommandBuilder<() -> Unit> {
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
        currentScreen = getOrSaveActiveScreen(newBackStack.last(), newState)
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

  private fun getOrSaveActiveScreen(path: Path, flowState: FlowState): Screen? {
    return activeScreens[path]
      ?: flowState.screenNodeSpecs[path.lastSegment]?.factory?.invoke(flowEventSink)?.also { activeScreens[path] = it }
  }

  private fun cleanUpActiveScreens(backStack: BackStack) {
    activeScreens.forEach { (path, screen) ->
      if (path !in backStack) {
        screen.destroy()
      }
    }
  }
}
