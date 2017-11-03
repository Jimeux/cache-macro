package mac

import scala.concurrent.Future

trait Cache {
  def set(key: String, value: String): Future[Unit]
  def get(key: String): Future[Option[String]]
}
