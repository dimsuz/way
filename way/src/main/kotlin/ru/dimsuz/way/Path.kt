package ru.dimsuz.way

@JvmInline
value class Path private constructor(private val segments: List<NodeKey>) {

  constructor(segment: NodeKey) : this(listOf(segment))
  constructor(
    head: NodeKey,
    tail: List<NodeKey>
  ) : this(ArrayList<NodeKey>(1 + tail.size).apply { add(head); addAll(tail) })

  companion object {
    fun fromNonEmptyListOf(segments: List<NodeKey>): Path {
      require(segments.isNotEmpty()) { "expected non-empty segment list" }
      return Path(segments)
    }
  }

  val lastSegment: NodeKey get() {
    return segments.lastOrNull() ?: error("internal error: empty path")
  }

  val firstSegment: NodeKey get() {
    return segments.firstOrNull() ?: error("internal error: empty path")
  }

  val tail: Path? get() {
    return if (segments.size == 1) null else fromNonEmptyListOf(segments.drop(1))
  }

  val parent: Path? get() = dropLast(1)

  val size get() = segments.size

  fun take(count: Int): Path {
    return fromNonEmptyListOf(segments.take(count))
  }

  fun drop(count: Int): Path? {
    return if (count >= segments.size) null else fromNonEmptyListOf(segments.drop(count))
  }

  fun dropLast(count: Int): Path? {
    return if (segments.size <= count) null else fromNonEmptyListOf(segments.dropLast(count))
  }

  infix fun append(segment: NodeKey): Path {
    return Path(this.segments.plus(segment))
  }

  override fun toString(): String {
    return segments.joinToString(".") { it.key }
  }

  fun asIterable(): Iterable<NodeKey> {
    return segments
  }
}

infix fun NodeKey.append(other: NodeKey): Path {
  return Path(this).append(other)
}
