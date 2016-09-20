package strd.net.http

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

import com.codahale.metrics._

case class HttpMetrics( registry : MetricRegistry,
                        prefix : String,
                        rps : Meter,
                        timeouts : Counter,
                        activeChannels : Counter,
                        ioExceptions : Counter,
                        successRequests : Counter,
                        failedRequests : Counter,
                        queueTimeouts : Counter,
                        unorderedRequests : Counter,
                        keepAliveRequests : Counter,
                        keepAliveTime : Histogram,
                        requestTime : Histogram) {

  val byStatusCode = new ConcurrentHashMap[Int, Counter]()

  def unorderedReq(): Unit = {
    unorderedRequests.inc()
  }

  def queueTimeout(): Unit = {
    queueTimeouts.inc()
  }

  def time(time: Long) = {
    requestTime.update(time.toInt)
  }

  def response(code : Int) : Unit = {
    byStatusCode.computeIfAbsent(code, new Function[Int,Counter] {
      override def apply(t: Int): Counter = {
        registry.counter(s"prefix.status.$t")
      }
    })
  }

  def deadline() : Unit = {
    timeouts.inc()
  }

  def request() : Unit = {
    rps.mark()
  }

  def handler(success: Boolean) {
    if (success) {
      successRequests.inc()
    } else {
      failedRequests.inc()
    }
  }


  def keepAlive(count: Int, time: Long) {
    keepAliveRequests.inc(count)
    keepAliveTime.update(time.toInt)
  }

}
object HttpMetrics {
  def apply(registry : MetricRegistry, prefix : String = "http."): HttpMetrics =
    HttpMetrics(
      registry,
      prefix,
      rps               = registry.meter(s"${prefix}rps"),
      timeouts          = registry.counter(s"${prefix}timeouts"),
      activeChannels    = registry.counter(s"${prefix}activeChannels"),
      ioExceptions      = registry.counter(s"${prefix}ioExceptions"),
      successRequests   = registry.counter(s"${prefix}successRequests"),
      failedRequests    = registry.counter(s"${prefix}failedRequests"),
      queueTimeouts     = registry.counter(s"${prefix}queueTimeouts"),
      unorderedRequests = registry.counter(s"${prefix}unorderedRequests"),
      keepAliveRequests = registry.counter(s"${prefix}keepAliveRequests"),
      keepAliveTime     = registry.histogram(s"${prefix}keepAliveTime"),
      requestTime       = registry.histogram(s"${prefix}requestTime")
    )
}