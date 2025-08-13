package pbyrne84.browsertesting.models

import java.time.Instant
import java.util.UUID

case class User(id: UUID, name: String, created: Instant, updated: Instant)
