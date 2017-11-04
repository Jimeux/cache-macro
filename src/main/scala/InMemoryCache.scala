import mac.Cache
import scala.concurrent.Future

class InMemoryCache extends Cache {

  private val cacheMap = scala.collection.mutable.Map[String, String]()

  override def set(key: String, value: String): Future[Unit] = {
    println(s"Cache[$key] SET $value")
    Future.successful(cacheMap.put(key, value))
  }

  override def get(key: String): Future[Option[String]] = {
    val value = cacheMap.get(key)
    println(s"Cache[$key] GET $value")
    Future.successful(value)
  }

}
