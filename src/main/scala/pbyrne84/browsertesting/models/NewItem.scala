package pbyrne84.browsertesting.models

import java.util.UUID

case class NewItem(userId: UUID, title: String, briefDescription: String, description: String, price: Int)
