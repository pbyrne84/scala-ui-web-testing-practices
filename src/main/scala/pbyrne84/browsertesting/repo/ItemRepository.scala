package pbyrne84.browsertesting.repo

import pbyrne84.browsertesting.models.*
import slick.jdbc.PostgresProfile

import java.time.{Clock, Instant}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ItemRepository(
    db: PostgresProfile.backend.JdbcDatabaseDef,
    clock: Clock,
    uuidProvider: () => UUID
)(implicit ec: ExecutionContext) {
  import slick.jdbc.PostgresProfile.api.*

  def create(newItem: NewItem): Future[Either[Throwable, Item]] = {
    val now  = Instant.now(clock)
    val uuid = uuidProvider()
    val item =
      Item(
        id = uuid,
        userId = newItem.userId,
        title = newItem.title,
        briefDescription = newItem.briefDescription,
        description = newItem.description,
        price = newItem.price,
        created = now,
        updated = now
      )

    db.run(Tables.itemTable += item)
      .flatMap { _ =>
        get(uuid).map {
          case Some(item) => Right(item)
          // could just return the item instead.
          case None => Left(new RuntimeException(s"Record $uuid not found after inserting"))
        }
      }
  }

  def remove(id: UUID): Future[Boolean] = {
    db.run(
      Tables.itemTable.filter(_.id === id).delete
    ).map(deletedRowCount => deletedRowCount > 0)
  }

  def get(id: UUID): Future[Option[Item]] = {
    val query = Tables.itemTable.filter(_.id === id).result.headOption

    /*
      query.statements.foreach(println)

      select
          "id",
         "user_id",
         "title",
         "brief_description",
         "description",
         "price",
         "created",
         "updated"
        from "item"
        where "id" = '35eb6dec-613b-447e-a886-aaf934a832c5'
     */

    db.run(query)
  }

  def search(searchTerm: String, startOffset: Int, limit: Int): Future[SearchResults] = {
    // In reality we would use something like ElasticSearch to search. Keep the data in a relational format
    // to guarantee some sort of structural integrity and then mirror in a search technology.
    // NoSql is at risk of being quite chaotic data structure wise and normal SQL in my mind guarantees greater sanity.
    // Data is often the most important thing in a project so it pays not to allow the wrong type of short cuts.

    // We would also sort by score of the search with a secondary stable value in the sort for when the things have the same
    // score. If we do not do this than items will bounce around in the search results leaving things missing when
    // going across pagination boundaries. Amazon is a good example of things bouncing around leaving you wondering
    // if the thing you wanted has just been lost in the pagination. Amazon has a web scale issue meaning this is not
    // as easy an issue to fix
    //
    // In usual databases we would use fulltext e.g. https://www.postgresql.org/docs/current/textsearch.html

    /*
      select "id",
             "user_id",
             "title",
             "brief_description",
             "description",
             "price",
             "created",
             "updated"
      from "item"
      where (("title" like '%computers%') or ("description" like '%computers%'))
         or ("brief_description" like '%computers%')
      order by "updated"
      limit 1 offset 0
     */

    val resultsQuery = (for {
      item <- Tables.itemTable
      // Slick is not that well documented and seems to always skip examples of what you need.
      // It makes some things easy and other things very hard to impossible (dynamic column count in an update etc.).
      // The danger of ORM's. It is often easier to write the SQL as it is far easier to tune
      if item.title.like(s"%$searchTerm%") || item.description.like(s"%$searchTerm%") || item.briefDescription.like(
        s"%$searchTerm%"
      )
    } yield item).sortBy(_.updated).drop(startOffset).take(limit)

    // ElasticSearch calculates this for free, but as we are using a DB we need to do a count with no limit
    // We need the total to do pagination calculations

    /*
      countQuery.result.statements.toList.foreach(println)

      select count(1)
      from "item"
      where (("title" like '%computers%') or ("description" like '%computers%'))
         or ("brief_description" like '%computers%')
     */
    val countQuery = (for {
      item <- Tables.itemTable
      // Slick is not that well documented and seems to always skip examples of what you need.
      // It makes some things easy and other things very hard to impossible (dynamic column count in an update etc.).
      // The danger of ORM's. It is often easier to write the SQL as it is far easier to tune
      if item.title.like(s"%$searchTerm%") || item.description.like(s"%$searchTerm%") || item.briefDescription.like(
        s"%$searchTerm%"
      )
    } yield item).size

    for {
      items <- db.run(resultsQuery.result)
      count <- db.run(countQuery.result)

    } yield {
      val searchResults = items.map(SearchResult.fromItem).toList
      SearchResults(searchTerm, count, (startOffset, startOffset + limit - 1), searchResults)
    }

  }

}
