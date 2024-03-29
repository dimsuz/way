import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey

data class LoginFlowState(
  val onEntryCalled: Boolean = false,
  val isDisposed: Boolean = false,
  val timer: Long = 0,
)

enum class LoginFlowResult {
  Success,
  Dismissed
}

data class PermissionsFlowState(
  val allGranted: Boolean = false,
)

enum class PermissionFlowResult {
  Success,
  Dismissed
}

@JvmInline
value class UserId(val value: String)

fun main() {
  val loginFlow = FlowNodeBuilder<LoginFlowState, UserId, LoginFlowResult>()
    .onEntry {
      updateState { s -> s.copy(onEntryCalled = true) }
    }
    .onExit {
      updateState { s -> s.copy(isDisposed = true) }
    }
    .addScreenNode(NodeKey("login_by_phone")) { screenBuilder ->
      screenBuilder
        .onEntry { println("entered $path") }
        .onExit { println("exited $path") }
        .on(Event.Name("CANT_LOGIN_REQUESTED")) {
          navigateTo(NodeKey("cant_login"))
        }
        .on(Event.Name("FINGERPRINT_SET")) {
          navigateTo(NodeKey("permissions"))
        }
        .on(Event.Name("GENERIC_EVENT")) {
          println("doing some generic side effect (printing stuff) and nothing more")
        }
        .on(Event.Name("GENERIC_EVENT1")) {
          updateState { s -> s.copy(timer = 33) }
        }
        .on(Event.Name.BACK) {
          finish(LoginFlowResult.Dismissed)
        }
        .build()
    }
    .addFlowNode<PermissionFlowResult>(NodeKey("permissions")) { subFlowBuilder ->
      subFlowBuilder
        .of(
          FlowNodeBuilder<PermissionsFlowState, Unit, PermissionFlowResult>()
            .build(PermissionsFlowState())
            .unwrap()
        )
        .onFinish {
          when (result) {
            PermissionFlowResult.Success -> finish(LoginFlowResult.Success)
            PermissionFlowResult.Dismissed -> finish(LoginFlowResult.Dismissed)
          }
        }
        .build()
        .unwrap()
    }
    .build(LoginFlowState())

  println("login flow: $loginFlow")
}
