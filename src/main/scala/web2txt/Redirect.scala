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

import scala.concurrent.Future

import spray.client.pipelining._
import spray.http._

import web2txt.Cookies.{addCookies,mergeCookies}

object Redirect {

  implicit val system = Main.system

  import system.dispatcher

  /**  spray-client does not follow redirects by default, so a custom SendReceive function must be used
    *  Inspired by suggestions in the following topics:
    *  https://groups.google.com/forum/#!topic/spray-user/VSqrUiTqtDg/discussion
    *  https://groups.google.com/forum/#!topic/spray-user/L7kiISZjtO8/discussion
    */
  def sendReceiveWithRedirects(sendReceive: SendReceive, maxRedirects: Int = 5): SendReceive = {request: HttpRequest =>

    def getUri(response: HttpResponse): Option[Uri] = {
      response.header[HttpHeaders.Location].map{_.uri}
    }

    def isRedirect(response: HttpResponse): Boolean = {
      response.status != StatusCodes.OK && (
        response.status == StatusCodes.Found ||
        response.status == StatusCodes.SeeOther ||
        response.status == StatusCodes.MovedPermanently ||
        response.status == StatusCodes.TemporaryRedirect ||
        response.status == StatusCodes.PermanentRedirect ||
        response.status == StatusCodes.UseProxy
      )
    }

    def sendReceiveFollowingRedirects(redirectsLeft: Int, request: HttpRequest): Future[HttpResponse] = {
      if (redirectsLeft <= 0){
        Future.successful(HttpResponse(StatusCodes.LoopDetected))
      } else {
        val response: Future[HttpResponse] = sendReceive(request)

        // Use flatMap to extract HttpResponse from Future[Future[HttpResponse]] that results from recursive method call
        response.flatMap {r =>
          if (!isRedirect(r)) {
            Future.successful(
              HttpResponse(
                StatusCodes.OK,
                entity = r.entity
              )
            )
          } else {
            val uri = getUri(r)
            if (uri.isEmpty) {
              // Wrap HttpResponse in a future to match what is returned by recursive dispatch call
              // Normally we would just return HttpResonse here but the future will be unwrapped by flatMap
              Future.successful(
                HttpResponse(
                  StatusCodes.NoContent
                )
              )
            } else {
              val cookies: Option[Seq[HttpCookie]] = mergeCookies(request,r)
              val newHeaders: List[HttpHeader] = addCookies(request.headers, cookies)
              val newRequest = request.copy(uri = uri.get, headers = newHeaders)

              // Recursively request the location specified by a redirect
              sendReceiveFollowingRedirects(redirectsLeft - 1, newRequest)
            }
          }
        }
      }
    }

    sendReceiveFollowingRedirects(maxRedirects, request)
  }

}
