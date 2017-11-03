import mac.cached

object Main extends App {

  @cached
  def test: String = "hello"

}
