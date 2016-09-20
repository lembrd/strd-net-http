package strd.net.http

import java.io.IOException

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

trait ChannelHandler extends SimpleChannelInboundHandler[HttpReq] {
  lazy val log = LoggerFactory.getLogger(getClass)
  def metrics : HttpMetrics

  metrics.activeChannels.inc()

  def handle(ctx: ChannelHandlerContext, req: HttpReq)

  def channelRead0(ctx: ChannelHandlerContext, req: HttpReq) {
    metrics.request()
    try {
      handle(ctx, req)
    } catch {
      case NonFatal(x:Exception) =>
        this.exceptionCaught(ctx, x)
    }
  }

  override def channelUnregistered(ctx: ChannelHandlerContext) = {
    super.channelUnregistered(ctx)
    metrics.activeChannels.dec()
  }

  override def channelReadComplete(ctx: ChannelHandlerContext) = {
    super.channelReadComplete(ctx)
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = {
    super.channelReadComplete(ctx)
    cause match {
      case x: IOException => metrics.ioExceptions.inc()
      case x: Exception => log.warn("Http Channel error", x)
    }
    ctx.close()
  }
}


//object ChannelHandler {
//  val log = LoggerFactory.getLogger(getClass)

/*
  val rps = new TimerStats {
    override def onTimer(counter: TimerCounter, n: Long) {
      if (n % 20 == 0) {
        log.debug("HttpServer RPS: " + counter.perSecond)
      }
    }
  }
*/
//  val rps =
//  val channels = new AtomicInteger()
//
//  HttpStats.channels { channels.get() }
//  HttpStats.rps { rps.get() }
//}

class DefaultChannelHandler(handler: RequestHandler,
                            val metrics : HttpMetrics,
                            queryFactory: (ChannelHandlerContext, HttpReq) => HttpStream)
                           (implicit execctx: ExecutionContext) extends ChannelHandler {

  private var stream: Option[HttpStream] = None

  private def getStream(ctx: ChannelHandlerContext, req: HttpReq) = {
    stream.getOrElse {
      val q = queryFactory(ctx, req)
      stream = Some(q)
      q
    }
  }

  def handle(ctx: ChannelHandlerContext, req: HttpReq) {
    val stream = getStream(ctx, req)

    stream.startRequest(req)
    try {
      handler.handle(req).onComplete {
        case Success(resp) =>
          metrics.handler(success = true)
          stream.sendResponse(req.id, resp)

        case Failure(b: BadRequest) =>
          metrics.handler(success = false)
          stream.sendResponse(req.id, HttpResp(status = b.status, body = b.msg.map(Content.apply)))

        case Failure(e) =>
          metrics.handler(success = false)
          log.warn("Unexpected Http error", e)
          stream.sendResponse(req.id, HttpResp(status = HttpResponseStatus.INTERNAL_SERVER_ERROR))
      }
    } catch {
      case x:Exception =>
        metrics.handler(success = false)
        log.warn("Unexpected Http.handle error", x)
        stream.sendResponse(req.id, HttpResp(status = HttpResponseStatus.INTERNAL_SERVER_ERROR))
    }
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    super.channelInactive(ctx)
    stream.foreach(_.stop())
  }
}
