# strd-net-http
STRD Http Library

Tiny Http library based on io.netty for high performance HTTP servers 

## How to use
build.sbt
```scala

resolvers += Resolver.bintrayRepo("lembrd", "maven")
libraryDependencies += "org.strd" %% "strd-net-http" % "0.9"
```

Code sample:
```scala

  implicit val execctx = ExecutionContext.fromExecutor( Executors.newCachedThreadPool() )
  val handlerTest = new RequestHandler {

    def handle(req: HttpReq): Future[HttpResp] = Future {
      log.debug(s"Request: $req")
      HttpResp(
        body = Some(Content("Hello")),
        headers = Map("X-Test" -> Seq("test"))
      )
    }
  }

  HttpServer.default(handlerTest,
    executionContext = execctx,
    HttpServerOptions( port = 8880)).start()

```
