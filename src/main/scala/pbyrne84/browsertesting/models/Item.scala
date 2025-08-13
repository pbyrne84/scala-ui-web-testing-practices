package pbyrne84.browsertesting.models

import java.time.Instant
import java.util.UUID

case class Item(
    id: UUID,
    userId: UUID,
    title: String,
    briefDescription: String,
    description: String,
    price: Int,
    created: Instant,
    updated: Instant
)
