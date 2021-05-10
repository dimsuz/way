import ru.dimsuz.way.FlowBuilder
import ru.dimsuz.way.NodeId

data class LoginFlowState(
  val onEntryCalled: Boolean = false,
  val isDisposed: Boolean = false,
)

@JvmInline
value class UserId(val value: String)

fun main() {
  val loginFlow = FlowBuilder<LoginFlowState, UserId>(NodeId("login"))
    .onEntry {
      updateState { s -> s.copy(onEntryCalled = true) }
    }
    .onExit {
      updateState { s -> s.copy(isDisposed = true) }
    }
    .addScreen(NodeId("login_by_phone")) { screenBuilder ->
      screenBuilder
    }
    .build(LoginFlowState())

  println("login flow: $loginFlow")
}
