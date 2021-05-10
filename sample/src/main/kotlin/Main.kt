import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowBuilder
import ru.dimsuz.way.NodeId

data class LoginFlowState(
  val onEntryCalled: Boolean = false,
  val isDisposed: Boolean = false,
  val timer: Long = 0,
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
        .onEntry { println("entered $path") }
        .onExit { println("exited $path") }
        .on(Event("CANT_LOGIN_REQUESTED")) {
          navigateTo(NodeId("cant_login"))
        }
        .on(Event("GENERIC_EVENT")) {
          println("doing some generic side effect (printing stuff) and nothing more")
        }
        .on(Event("GENERIC_EVENT1")) {
          updateState { s -> s.copy(timer = 33) }
        }
        .on(Event.BACK) {
          finish()
        }
        .build()
    }
    .build(LoginFlowState())

  println("login flow: $loginFlow")
}
