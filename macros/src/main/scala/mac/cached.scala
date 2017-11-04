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
        abort("@cached annotation only works on `def`")
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
  def applyCache[A <: AnyRef : Manifest](key: String, retrieveFn: => Future[A])
    (implicit cache: Cache, ec: ExecutionContext, formats: Formats): Future[A] =
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

  private def performSafely[A](f: => A): Option[A] =
    Try(f) match {
      case Success(a) =>
        Some(a)
      case Failure(e) =>
        e.printStackTrace()
        None
    }

}

object CachedExpander {

  private val SignatureError = "@cached must annotate a non-abstract `def` with zero or more parameters " +
    "that returns type `scala.concurrent.Future[A]`"
  private val ReturnTypeError = "@cached must annotate a `def` with a return type of `scala.concurrent.Future[A]`"

  def apply(annotatedDef: Defn.Def): Defn.Def = {
    annotatedDef match {
      case q"..$_ def $fnName(...$params): ${rtType: Option[Type]} = $expr" =>
        if (invalidReturnType(rtType)) abort(ReturnTypeError)
        else {
          val valueType = Type.Name(rtType.get.children(1).syntax)
          val func = Term.Name("\"" + fnName.syntax + "\"")
          val paramString = paramKeys(params)
          val paramList = params.headOption.getOrElse(Seq[Term.Param]())
          q"""
            def $fnName(..$paramList): $rtType = {
              val key = getClass.getName + ":" + $func + ":" + $paramString
              mac.cached.applyCache[$valueType](key, $expr)
            }
          """
        }

      case _ => abort(SignatureError)
    }
  }

  private def invalidReturnType(returnType: Option[Type]): Boolean =
    returnType.exists { rt =>
      rt.children.size != 2 || rt.children.head.toString != "Future"
    }

  private def paramKeys(paramLists: Seq[Seq[Term.Param]]): Term =
    paramLists.headOption
      .map { firstList => firstList map paramToKey }
      .map { keys => Term.Apply(Term.Name("String.valueOf"), keys.toList) }
      .getOrElse(Term.Name("\"\""))

  private def paramToKey(param: Term.Param): Term = {
    val symbol = param.name.syntax
    s""""$symbol:" + $symbol.toString""".parse[Term].get
  }

}
