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
          val paramList = params.headOption.getOrElse(Seq[Term.Param]())
          val fnNameKeyString = Term.Name("\"" + fnName.syntax + "\"")
          val paramKeyString = paramKeys(params)
          val valueType = Type.Name(rtType.get.children(1).syntax)

          q"""
            def $fnName(..$paramList): $rtType = {
              val key = getClass.getName + ":" + $fnNameKeyString + ":" + $paramKeyString
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
