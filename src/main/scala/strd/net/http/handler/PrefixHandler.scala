package strd.net.http.handler

import strd.net.http._

import scala.concurrent.Future

/**
 * @author Kirill chEbba Chebunin
 */
class PrefixHandler(prefix: String, nested: RequestHandler) extends PartialRequestHandler {
  object Prefix {
    def unapply(req: HttpReq) = {
      if (req.path.startsWith(prefix)) {
        Some(req.path.substring(prefix.length, prefix.length))
      } else {
        None
      }
    }
  }

  def tryHandle: PartialFunction[HttpReq, Future[HttpResp]] = {
    case req @ Prefix(suffix) =>
      nested.handle(new HttpReq {
        def ip = req.ip

        def uri = req.uri

        override lazy val path = suffix

        def method = req.method

        def keepAlive = req.keepAlive

        def content = req.content

        def headers = req.headers

        def started = req.started

        def id = req.id
      })
  }
}
