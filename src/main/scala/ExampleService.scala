import mac.{Cache, cached}
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}

import scala.concurrent.{ExecutionContext, Future}

class ExampleService(
  repository: ExampleRepository,
  implicit val cache: Cache,
  implicit val ec: ExecutionContext
) {

  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  @cached
  def findById(id: Int): Future[Option[Example]] =
    repository.findById(id)

  @cached
  def findAll: Future[Seq[Example]] =
    repository.findAll

}
