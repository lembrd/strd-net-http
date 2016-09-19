package strd.net.http

import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

import io.netty.handler.codec.http._

import scala.collection.convert.decorateAsScala._
import scala.util.Try

case class HttpReqImpl(id: Long, started: Long, method: HttpMethod, uri: String, headers: MultiStringMap, content: ByteBuffer, keepAlive: Boolean, ip: String) extends HttpReq

trait HttpReq {
  def id: Long
  def started: Long
  def method: HttpMethod
  def uri: String
  def headers: MultiStringMap
  def content: ByteBuffer
  def keepAlive: Boolean

  def ip: String

  lazy val (path, getQuery) = {
    val decoder = new QueryStringDecoder(uri)
    (decoder.path().dropWhile(_ == '/'), MultiStringMap(decoder.parameters()))
  }

  lazy val query: MultiStringMap = if (method == HttpMethod.POST) contentParams else getQuery
  lazy val contentParams: MultiStringMap = Try { MultiStringMap(_params) }.getOrElse(Map.empty)


  lazy val cookies = {
    headers.get(HttpHeaders.Names.COOKIE).map { values =>
      values.flatMap { c =>
        Try(CookieDecoder.decode(c).asScala).getOrElse(Set.empty[Cookie])
      }.map { c =>
        c.getName -> c.getValue
      }.toMap
    }.getOrElse(Map.empty)
  }

  def processingTime = System.currentTimeMillis() - started


  lazy private val _params = new QueryStringDecoder(contentString, false).parameters()

  lazy val contentBytes =  {
    val b = new Array[Byte](content.limit())
    content.get(b)
    b
  }
  def contentString = new String(contentBytes, Charset.forName("UTF-8"))

  def contentStream = new ByteArrayInputStream(contentBytes) // TODO: use some native byte buf input stream


  def host      : Option[String] = headers.getOne(HttpHeaders.Names.HOST)
  def userAgent : Option[String] = headers.getOne(HttpHeaders.Names.USER_AGENT)
  def referrer  : Option[String] = headers.getOne(HttpHeaders.Names.REFERER)
  def schema    : String = headers.getOne("X-Forwarded-Proto").orElse(headers.getOne("X_FORWARDED_PROTO")).getOrElse("http")

  def hostAndSchemaOpt : Option[HostWithSchema] = host.map( HostWithSchema(schema, _) )
  def hostAndSchema : HostWithSchema = host.map( HostWithSchema(schema, _) ).getOrElse(EmptyHostWithSchema)

  override def toString =
    s"""
      |HttpReq: $id $started $ip
      |$method $uri
      |${headers.toSeq.flatMap(h => h._2.map(v => s"${h._1}: $v")).mkString("\n")}
      |
      |${content.limit()} bytes
    """.stripMargin.trim
}

case class HostWithSchema(schema : String, host : String) {
  override def toString = s"$schema://$host"
}

object EmptyHostWithSchema extends HostWithSchema("","") {
  override def toString : String = ""
}