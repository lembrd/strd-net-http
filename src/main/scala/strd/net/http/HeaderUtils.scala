package strd.net.http

import java.util.{Date, Locale}

import io.netty.handler.codec.http._
import org.apache.commons.lang.time.{DateFormatUtils, DateUtils}

/**
 * @author Kirill chEbba Chebunin
 */
trait HeaderUtils {
  val NoCache = HttpHeaders.Names.CACHE_CONTROL -> Seq("no-cache,no-store,max-age=0,must-revalidate")

  val DISABLE_CACHE = Map(
    NoCache,
    HttpHeaders.Names.PRAGMA        -> Seq(HttpHeaders.Values.NO_CACHE),
    HttpHeaders.Names.EXPIRES       -> Seq(DateFormatUtils.formatUTC(DateUtils.addSeconds(new Date(), 0), "EEE, dd MMM yyyy kk:mm:ss z", Locale.US)),
    HttpHeaders.Names.DATE          -> Seq(DateFormatUtils.formatUTC(new Date, "EEE, dd MMM yyyy kk:mm:ss z", Locale.US))
  )

  val ENABLE_CACHE = Map(
    HttpHeaders.Names.CACHE_CONTROL -> Seq("max-age=31556926")
  )

  val KeepAlive = HttpHeaders.Names.CONNECTION -> Seq(HttpHeaders.Values.KEEP_ALIVE)
  val Close = HttpHeaders.Names.CONNECTION -> Seq(HttpHeaders.Values.CLOSE)

  def Cookies(cookies: Cookie*): MultiString = HttpHeaders.Names.SET_COOKIE -> cookies.map(ServerCookieEncoder.encode)
}
