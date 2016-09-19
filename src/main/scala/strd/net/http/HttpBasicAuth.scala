package strd.net.http

import java.nio.charset.Charset

import io.netty.buffer.Unpooled
import io.netty.handler.codec.base64.Base64
import io.netty.handler.codec.http._

import scala.concurrent.Future

/**
 *
 * User: lembrd
 * Date: 11/05/15
 * Time: 18:45
 */


object HttpBasicAuth {
  def unautorized = {
    HttpResp( HttpResponseStatus.UNAUTHORIZED,
      headers = Map(HttpHeaders.Names.WWW_AUTHENTICATE -> Seq("Basic realm=\"bad user or password\"")),
      body = Some( Content("not authorized") ))
  }

  def authSync(http: HttpReq, usersPass: Seq[String], process: HttpReq => HttpResp): HttpResp = {
    http.headers.getFirst(HttpHeaders.Names.AUTHORIZATION).map( header => {
      if (header.contains("Basic ")) {
        val auth = header.replace("Basic ", "")
        val pass = Base64.decode( Unpooled.wrappedBuffer(auth.getBytes) ).toString(Charset.forName("UTF8") )
        if (usersPass.contains(pass)) {
          process(http)
        } else {
          unautorized
        }
      } else {
        unautorized
      }
    }) getOrElse unautorized
  }

  def authAsync(http: HttpReq, usersPass: Seq[String], process: HttpReq => Future[HttpResp]): Future[HttpResp] = {
    http.headers.getFirst(HttpHeaders.Names.AUTHORIZATION).map( header => {
      if (header.contains("Basic ")) {
        val auth = header.replace("Basic ", "")
        val pass = Base64.decode( Unpooled.wrappedBuffer(auth.getBytes) ).toString(Charset.forName("UTF8") )
        if (usersPass.contains(pass)) {
          process(http)
        } else {
          Future.successful(unautorized)
        }
      } else {
        Future.successful(unautorized)
      }
    }) getOrElse Future.successful(unautorized)
  }

}


