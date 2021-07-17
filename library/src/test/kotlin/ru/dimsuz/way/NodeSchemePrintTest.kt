package ru.dimsuz.way

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.scheme

class NodeSchemePrintTest : ShouldSpec({

  should("correctly print atomic scheme") {
    val scheme = scheme(
      initial = "nodeA",
      node(
        "nodeA",
        on("X", "nodeB"),
        on("Y", "nodeC"),
      ),
      node("nodeB", on("X", "nodeC")),
      node("nodeC"),
      node("nodeNodeZ"),
    )
    scheme.toString() shouldBe """
┌─────────┬────────┬──────┐
│STATE    │ON EVENT│TARGET│
├─────────┼────────┴──────┤
│initial  │nodeA          │
├─────────┼────────┬──────┤
│nodeA    │X       │nodeB │
│         ├────────┼──────┤
│         │Y       │nodeC │
├─────────┼────────┼──────┤
│nodeB    │X       │nodeC │
├─────────┼────────┴──────┤
│nodeC    │no transitions │
├─────────┼───────────────┤
│nodeNodeZ│no transitions │
└─────────┴───────────────┘
    """.trimIndent()
  }

  should("correctly print 1-level deep compound scheme") {
    val scheme = scheme(
      initial = "nodeA",
      node(
        "nodeA",
        on("X", "nodeB"),
        on("Y", "nodeC"),
      ),
      node("nodeB", on("X", "nodeC")),
      node(
        "nodeC",
        scheme(
          initial = "nodeA",
          node(
            "nodeA",
            on("X", "nodeB"),
            on("Y", "nodeC"),
          ),
          node("nodeB", on("X", "nodeC")),
          node("nodeC"),
          node("nodeNodeZ")
        )
      ),
      node("nodeNodeZ"),
    )
    scheme.toString() shouldBe """
┌─────────┬─────────┬───────┐       
│STATE    │ON EVENT │TARGET │       
├─────────┼─────────┴───────┤       
│initial  │nodeA            │       
├─────────┼─────────┬───────┤       
│nodeA    │X        │nodeB  │       
│         ├─────────┼───────┤       
│         │Y        │nodeC  │       
├─────────┼─────────┼───────┤       
│nodeB    │X        │nodeC  │       
├─────────┼─────────┴───────┴──────┐
│nodeC    │COMPOUND NODE           │
├─────────┼─────────┬──────────────┤
│         │initial  │nodeA         │
│         ├─────────┼───────┬──────┤
│         │nodeA    │X      │nodeB │
│         │         ├───────┼──────┤
│         │         │Y      │nodeC │
│         ├─────────┼───────┼──────┤
│         │nodeB    │X      │nodeC │
│         ├─────────┼───────┴──────┤
│         │nodeC    │no transitions│
│         ├─────────┼──────────────┤
│         │nodeNodeZ│no transitions│
├─────────┼─────────┴───────┬──────┘
│nodeNodeZ│no transitions   │       
└─────────┴─────────────────┘       
    """.trimIndent()
  }

  should("correctly print 2-level deep compound scheme") {
    val scheme = scheme(
      initial = "nodeA",
      node(
        "nodeA",
        on("X", "nodeB"),
        on("Y", "nodeC"),
      ),
      node("nodeB", on("X", "nodeC")),
      node(
        "nodeC",
        scheme(
          initial = "nodeA",
          node(
            "nodeA",
            on("X", "nodeB"),
            on("Y", "nodeC"),
          ),
          node(
            "nodeB",
            scheme(
              initial = "nodeA",
              node(
                "nodeA",
                on("X", "nodeB"),
                on("Y", "nodeC"),
              ),
              node("nodeB", on("X", "nodeC")),
              node("nodeC"),
              node("nodeNodeZ"),
            )
          ),
          node("nodeC", on("X", "nodeA")),
          node("nodeNodeZ")
        )
      ),
      node("nodeNodeZ"),
    )
    scheme.toString() shouldBe """
┌─────────┬─────────┬─────────┐                 
│STATE    │ON EVENT │TARGET   │                 
├─────────┼─────────┴─────────┤                 
│initial  │nodeA              │                 
├─────────┼─────────┬─────────┤                 
│nodeA    │X        │nodeB    │                 
│         ├─────────┼─────────┤                 
│         │Y        │nodeC    │                 
├─────────┼─────────┼─────────┤                 
│nodeB    │X        │nodeC    │                 
├─────────┼─────────┴─────────┴───────┐         
│nodeC    │COMPOUND NODE              │         
├─────────┼─────────┬─────────────────┤         
│         │initial  │nodeA            │         
│         ├─────────┼─────────┬───────┤         
│         │nodeA    │X        │nodeB  │         
│         │         ├─────────┼───────┤         
│         │         │Y        │nodeC  │         
├─────────┼─────────┼─────────┴───────┴────────┐
│         │nodeB    │COMPOUND NODE             │
├─────────┼─────────┼─────────┬────────────────┤
│         │         │initial  │nodeA           │
│         │         ├─────────┼───────┬──────┬─┘
│         │         │nodeA    │X      │nodeB │  
│         │         │         ├───────┼──────┤  
│         │         │         │Y      │nodeC │  
│         │         ├─────────┼───────┼──────┤  
│         │         │nodeB    │X      │nodeC │  
│         │         ├─────────┼───────┴──────┤  
│         │         │nodeC    │no transitions│  
│         │         ├─────────┼──────────────┤  
│         │         │nodeNodeZ│no transitions│  
│         ├─────────┼─────────┼───────┬──────┘  
│         │nodeC    │X        │nodeA  │         
│         ├─────────┼─────────┴───────┤         
│         │nodeNodeZ│no transitions   │         
├─────────┼─────────┴─────────┬───────┘         
│nodeNodeZ│no transitions     │                 
└─────────┴───────────────────┘                 
    """.trimIndent()
  }
})
