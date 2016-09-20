package strd.net

import io.netty.buffer.ByteBuf

import scala.util.matching.Regex

/**
 * @author Kirill chEbba Chebunin
 */
package object http extends MultiStringMapUtils
                            with HttpMethodAliases
                            with HeaderUtils {


  type MultiStringMap = Map[String, Seq[String]]
  type MultiString =  (String, Seq[String])

  implicit def stringToContent(str:String) : Option[ByteBuf] = Some(Content(str))

  implicit class RegexContext(sc: StringContext) {
    def r = new Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  val EMPTY_GIF = {
    val stream = Thread.currentThread().getContextClassLoader.getResourceAsStream("empty.gif")
    if (stream == null ) {
      throw new RuntimeException("Can not find resource :empty.gif")
    }
    val ba = new Array[Byte](stream.available())
    stream.read(ba)
    ba
  }
}
