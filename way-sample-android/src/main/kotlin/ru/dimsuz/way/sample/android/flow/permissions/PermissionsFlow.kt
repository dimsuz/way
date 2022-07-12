package ru.dimsuz.way.sample.android.flow.permissions

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.permissions.FlowEvent
import ru.dimsuz.way.sample.android.ui.permissions.screen.request.RequestScreen
import ru.dimsuz.way.sample.android.ui.permissions.screen.request.RequestViewModel

object PermissionsFlow {
  val key = NodeKey("permissions_flow")

  data class State(
    override val screens: Map<NodeKey, Screen> = emptyMap(),
  ) : FlowState

  enum class Result {
    Granted,
    Denied,
    Dismissed
  }

  fun buildNode(eventSink: FlowEventSink): FlowNode<State, Unit, Result> {
    return FlowNodeBuilder<State, Unit, Result>()
      .setInitial(NodeKey(RequestScreen.key))
      .addScreenNode(NodeKey(RequestScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("PermissionsFlow", "onEntry request")
            val viewModel = RequestViewModel(eventSink)
            updateState {
              it.copy(
                screens = state.screens.plus(
                  NodeKey(RequestScreen.key) to RequestScreen(
                    viewModel
                  )
                ),
              )
            }
          }
          .onExit {
            Log.d("PermissionsFlow", "onExit request")
            updateState {
              it.copy(
                screens = state.screens.minus(NodeKey(RequestScreen.key)),
              )
            }
          }
          .on(FlowEvent.Granted.name) {
            finish(Result.Granted)
          }
          .on(FlowEvent.Denied.name) {
            finish(Result.Denied)
          }
          .on(Event.Name.BACK) {
            finish(Result.Dismissed)
          }
          .build()
      }
      .build(State())
      .unwrap()
  }
}
