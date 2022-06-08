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
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.app.AppFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpScreen
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpViewModel

class MainActivity : ComponentActivity() {
  private val scope = CoroutineScope(Dispatchers.Main)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val eventSink = MutableSharedFlow<Event>()
    val commandBuilder = ComposableCommandBuilder { scope.launch { eventSink.emit(it) } }

    val navigationService = NavigationService(
      machine = NavigationMachine(AppFlow.buildNode()),
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
        var previousScreen by remember { mutableStateOf(commandBuilder.currentScreen) }
        commandBuilder.currentScreen?.Content()
        LaunchedEffect(commandBuilder.currentScreen) {
          commandBuilder.currentScreen?.onAttach()
        }
        DisposableEffect(commandBuilder.currentScreen) {
          onDispose {
            previousScreen?.onDetach()
            previousScreen = commandBuilder.currentScreen
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
  private val eventSink: FlowEventSink,
) : CommandBuilder<() -> Unit> {
  var currentScreen: Screen? by mutableStateOf(null)

  override fun invoke(oldBackStack: BackStack, newBackStack: BackStack): () -> Unit {
    return {
      println(buildString {
        appendLine("building command")
        append("  old backstack: $oldBackStack")
        append("  new backstack: $newBackStack")
      })
      currentScreen = when(newBackStack.lastOrNull()?.lastSegment) {
        NodeKey(CredentialsScreen.key) -> CredentialsScreen(CredentialsViewModel(eventSink))
        NodeKey(OtpScreen.key) -> OtpScreen(OtpViewModel(eventSink))
        else -> error("don't know how to make screen for a backstack: $newBackStack")
      }
      Log.d("builder", "curr screen $currentScreen")
    }
  }
}
