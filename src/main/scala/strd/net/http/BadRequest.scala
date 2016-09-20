package strd.net.http

import io.netty.handler.codec.http.HttpResponseStatus

/**
 *
 * User: lembrd
 * Date: 20/05/15
 * Time: 20:24
 */

case class BadRequest(status: HttpResponseStatus, msg: Option[String] = None) extends Exception
