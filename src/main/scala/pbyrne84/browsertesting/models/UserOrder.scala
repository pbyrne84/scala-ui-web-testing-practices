package pbyrne84.browsertesting.models

import java.time.Instant
import java.util.UUID

case class UserOrder(id: UUID, userId: UUID, created: Instant)
