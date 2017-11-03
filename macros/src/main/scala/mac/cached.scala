package mac

import scala.meta._
import collection.immutable.Seq

class cached extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case defn: Defn.Def =>
        this match {
          case q"new $_" => Expander.expand(defn)
          case x => abort(s"Unrecognized pattern $x")
        }
      case _ =>
        abort("@cached annotation only works on `def`")
    }
  }

}

object Expander {
  def expand(annotatedDef: Defn.Def): Defn.Def = {
    annotatedDef match {
      case q"..$_ def $fnName(...$params): ${rtType: Option[Type]} = $expr" =>
        if (invalidReturnType(rtType))
          abort(s"@cached method return type must be wrapped in scala.concurrent.Future")
        else {
          val valueType = Type.Name(rtType.get.children(1).toString)
          val func = Term.Name("\"" + fnName.syntax + "\"")
          val paramString = paramKeys(params)
          val paramList: Seq[Term.Param] = params.headOption.getOrElse(Seq[Term.Param]())
          q"""
            def $fnName(..$paramList): $rtType = {
              import org.json4s.native.Serialization.{read => json4sRead, write => json4sWrite}

              val key = getClass.getName + ":" + $func + ":" + $paramString

              def applyCache(implicit cache: Cache, formats: org.json4s.Formats): $rtType = {
                cache.get(key) flatMap {
                  case Some(cachedValue) =>
                    Future.successful(json4sRead[$valueType](cachedValue)) // TODO Error handling
                  case None =>
                    $expr map { retrievedValue =>
                      cache.set(key, json4sWrite[$valueType](retrievedValue))
                      retrievedValue
                    }
                }
              }

              applyCache
            }
          """
        }

      case other => abort(s"Expected non-curried method, got $other")
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
