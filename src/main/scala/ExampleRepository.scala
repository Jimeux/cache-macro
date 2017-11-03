import scala.concurrent.Future

class ExampleRepository {

  private val examples = scala.collection.mutable.Seq[Example](
    Example(1, "Example 1"),
    Example(2, "Example 2"),
    Example(3, "Example 3")
  )

  def findAll: Future[Seq[Example]] = Future.successful {
    examples
  }

  def findById(id: Int): Future[Option[Example]] = Future.successful {
    examples.find(_.id == id)
  }

}
