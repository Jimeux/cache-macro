package mac

import org.json4s.Formats
import org.json4s.native.Serialization.{read, write}

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.meta._
import scala.util.{Failure, Success, Try}

class cached extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    defn match {
      case defn: Defn.Def =>
        this match {
          case q"new $_" => CachedExpander(defn)
          case x => abort(s"Unrecognized pattern $x")
        }
      case _ =>
        abort("@cached must annotate a function defined with `def`")
    }
  }
}

object cached {
  /**
    * @param key        The key to be used to get/set the value
    * @param retrieveFn The expression that retrieves the uncached value
    * @param cache      The cache itself
    * @param ec         ExecutionContext for Futures
    * @param formats    json4s formats for (de)serialization
    * @tparam        A The type of the value to be (de)serialized
    * @return The cached value if available, else retrieve the uncached value
    */
  def applyCache[A <: AnyRef : Manifest](key: String,
                                         retrieveFn: => Future[A])(implicit
                                         cache: Cache,
                                         ec: ExecutionContext,
                                         formats: Formats): Future[A] = {
    cache.get(key) map { maybeRawValue =>
      maybeRawValue flatMap (rawValue => performSafely(read[A](rawValue)))
    } flatMap {
      case Some(value) =>
        Future.successful(value)
      case None =>
        retrieveFn map { retrievedValue =>
          performSafely(write[A](retrievedValue)) map (cache.set(key, _))
          retrievedValue
        }
    }
  }

  private def performSafely[A](f: => A): Option[A] =
    Try(f) match {
      case Success(a) =>
        Some(a)
      case Failure(e) =>
        e.printStackTrace()
        None
    }

}
