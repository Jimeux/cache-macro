package mac

import scala.meta._

class cached extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case defn: Defn.Def =>
        this match {
          case q"new $_" => defn
          case x => abort(s"Unrecognized pattern $x")
        }
      case _ =>
        abort("@cached annotation only works on `def`")
    }
  }

}
