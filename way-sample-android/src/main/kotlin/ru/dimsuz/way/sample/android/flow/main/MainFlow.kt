package ru.dimsuz.way.sample.android.flow.main

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.profile.ProfileFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.main.FlowEvent
import ru.dimsuz.way.sample.android.ui.main.screen.main.MainScreen
import ru.dimsuz.way.sample.android.ui.main.screen.main.MainViewModel

object MainFlow {
  val key = NodeKey("main_flow")

  data class State(
    override val screens: Map<NodeKey, Screen> = emptyMap(),
  ) : FlowState

  fun buildNode(eventSink: FlowEventSink): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey(MainScreen.key))
      .addScreenNode(NodeKey(MainScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("MainFlow", "onEntry Main")
            val viewModel = MainViewModel(eventSink)
            updateState {
              it.copy(
                screens = state.screens.plus(
                  NodeKey(MainScreen.key) to MainScreen(
                    viewModel
                  )
                ),
              )
            }
          }
          .onExit {
            Log.d("MainFlow", "onExit Main")
            updateState {
              it.copy(
                screens = state.screens.minus(NodeKey(MainScreen.key)),
              )
            }
          }
          .on(FlowEvent.ViewProfile.name) {
            navigateTo(ProfileFlow.key)
          }
          .on(Event.Name.BACK) {
            finish(FlowResult.Dismissed)
          }
          .build()
      }
      .addFlowNode<FlowResult>(ProfileFlow.key) { builder ->
        builder.of(ProfileFlow.buildNode(eventSink))
          .build()
          .unwrap()
      }
      .build(State())
      .unwrap()
  }
}
