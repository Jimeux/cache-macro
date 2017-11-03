
object Main extends App {

  val repository = new ExampleRepository
  val cache = new InMemoryCache
  val ec = scala.concurrent.ExecutionContext.Implicits.global
  val service = new ExampleService(repository, cache, ec)

  service.findById(1)
  service.findById(2)
  service.findAll
  Thread.sleep(500)
  println("-" * 100)
  service.findById(1)
  service.findById(2)
  service.findAll

}
