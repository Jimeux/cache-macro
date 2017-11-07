package mac

import scala.collection.immutable.Seq
import scala.meta._

object CachedExpander {

  private val SignatureError =
    "@cached must annotate a non-abstract `def` with zero or more parameters " +
      "that returns type `scala.concurrent.Future[A]`"
  private val ReturnTypeError =
    "@cached must annotate a `def` with a return type of `scala.concurrent.Future[A]`"

  def apply(annotatedDef: Defn.Def): Defn.Def = {
    annotatedDef match {
      case q"..$_ def $fnName(...$params): ${rtType: Option[Type]} = $expr" =>
        if (invalidReturnType(rtType))
          abort(ReturnTypeError)
        else {
          val firstParamList = params.headOption getOrElse Seq[Term.Param]()
          val key = generateKey(firstParamList, fnName)
          val valueType = Type.Name(rtType.get.children(1).syntax)

          q"""
            def $fnName(..$firstParamList): $rtType = {
              val key = $key
              mac.cached.applyCache[$valueType](key, $expr)
            }
          """
        }

      case _ => abort(SignatureError)
    }
  }

  private def invalidReturnType(returnType: Option[Type]): Boolean =
    returnType.exists { rt =>
      rt.children.size != 2 || rt.children.head.syntax != "Future"
    }

  private def generateKey(paramList: Seq[Term.Param], fnName: Term.Name): Term = {
    val paramKeys = paramList map { param =>
      val symbol = param.name.syntax
      s"$symbol:$$$symbol"
    } mkString ":"

    s"""s"$${getClass.getName}:${fnName.syntax}:$paramKeys"""".parse[Term].get
  }

}
