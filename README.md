# cache-macro

A macro example with [scala.meta](http://scalameta.org/) and [Macro Paradise](https://docs.scala-lang.org/overviews/macros/paradise.html).

# Usage

Annotate a `def` with `@cache` for basic caching functionality.

For example, the following usage:

```scala
@cache
def find(id: Int, includeAddress: Boolean): Future[Option[User]] = {
  userRepository.findById(id, includeAddress)
}
``` 

Expands to:

```scala
def find(id: Int, includeAddress: Boolean): Future[Option[User]] = {
  val key = s"${getClass.getName}:findById:id:$id:includeAddress:$includeAddress"
  mac.cached.applyCache[Option[User]](key, userRepository.findById(id, includeAddress))
}
```