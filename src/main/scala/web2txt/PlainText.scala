/*
 * Copyright (C) 2015 Jason Mar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package web2txt

import akka.util.Timeout
import spray.client.pipelining._
import spray.http._
import spray.httpx.encoding.{Deflate, Gzip}

import scala.concurrent.Future
import scala.concurrent.duration._

import web2txt.Headers.defaultHeaders
import web2txt.Redirect.sendReceiveWithRedirects

object PlainText {

  implicit val system = Main.system

  import system.dispatcher

  case class PlainTextResult(s: String) {
    override def toString = s
  }

  val decodePipeline: SendReceive = {
    sendReceive ~> decode(Gzip) ~> decode(Deflate)
  }

  val requestPipeline: SendReceive = { // SendReceive is an alias for HttpRequest => Future[HttpResponse]
    removeHeader[HttpHeaders.`User-Agent`] ~>
      addHeaders(defaultHeaders) ~> // Add User-Agent
      encode(Gzip) ~> // Gzip if supported
      sendReceiveWithRedirects(
        decodePipeline
      ) // Follow redirect chain if necessary
  }

  def getTextContentFromUrl(url: String, src: Boolean = false): Future[HttpResponse] = {
    implicit val timeout: Timeout = 142.second
    val plainTxtUnmarshaller = {
      if (src) {
        Unmarshalling.srcUnmarshaller(url)
      } else {
        Unmarshalling.plainTxtUnmarshaller(url)
      }
    }

    val httpRequest: HttpRequest = Get(url)
    val response: Future[HttpResponse] = requestPipeline(httpRequest)

    for {
      r <- response
    } yield {
      r.status match {
        case StatusCodes.OK =>
          plainTxtUnmarshaller(r.entity) match {
            case deserialized if deserialized.isRight =>
              HttpResponse(
                StatusCodes.OK,
                HttpEntity(
                  ContentTypes.`text/plain(UTF-8)`,
                  deserialized.right.get.toString
                )
              )
            case _ =>
              HttpResponse(StatusCodes.NoContent)
          }
        case _ =>
          HttpResponse(r.status)

      }
    }
  }

}