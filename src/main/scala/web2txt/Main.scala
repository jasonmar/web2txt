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

import akka.actor.ActorSystem
import spray.http._
import spray.httpx.encoding.{Deflate, Gzip, NoEncoding}
import spray.routing._

import web2txt.Cookies.{genCookie0,genCookie1}
import web2txt.PlainText._


object Main extends App with SimpleRoutingApp {

  implicit val system = ActorSystem()

  import system.dispatcher

  /**  Example of a custom Directive
   *   Extracts all headers for use in sub-directives
   */
  def getAllHeaders: Directive1[List[HttpHeader]] = {
    extract(_.request.headers)
  }

  /** This binds to localhost:8080 and listens for incoming http requests
   *
   */
  startServer("localhost", port = 8080) {
    decompressRequest(Gzip, NoEncoding, Deflate) {
      compressResponse () {
        get {
          pathSingleSlash {
            getFromResource("index.html",ContentType(MediaTypes.`text/html`,HttpCharsets.`UTF-8`))
          } ~
          pathPrefix("css") {getFromResourceDirectory("css")} ~
          pathPrefix("js") {getFromResourceDirectory("js")} ~
          path("txt" / RestPath) {uri =>
            complete {
              getTextContentFromUrl(uri.toString())
            }          
          } ~
          path("src" / RestPath) {uri =>
            complete {
              getTextContentFromUrl(uri.toString(),src = true)
            }
          } ~
          path("require-cookie" ) {
            handleRejections(
              RejectionHandler {
                case _ =>
                  complete {
                    <html>
                      <head>
                        <meta charset="UTF-8" />
                        <title>Web2Txt</title>
                        <link rel="stylesheet" href="/css/normalize.css" />
                        <link rel="stylesheet" href="/css/skeleton.css" />
                      </head>
                      <div class="container">
                        <h1>Cookies are required</h1>
                      </div>
                    </html>
                  }
              }
            ) {
              cookie (Cookies.cookie0Name) {cookie =>
                validate(Cookies.isValid(cookie), "test cookie is invalid") { // example of cookie validation
                  dynamic { // This is dynamic because the cookie expiration is generated at runtime
                    setCookie(genCookie1()) { // set the second cookie now that we know the client accepts cookies
                      redirect(
                        Uri("/cookies"), // in a real situation this would redirect to an actual destination page
                        StatusCodes.Found
                      )
                    }
                  }
                }
              }
            }
          } ~
          path("cookies" ) { // This shows the cookies sent with the request
            optionalHeaderValueByName("cookie"){cookie =>
              complete {
                <html>
                  <head>
                    <meta charset="UTF-8" />
                    <title>Web2Txt</title>
                    <link rel="stylesheet" href="/css/normalize.css" />
                    <link rel="stylesheet" href="/css/skeleton.css" />
                  </head>
                  <div class="container">
                    <h1>Cookies</h1>
                    <p>${cookie.toString}</p>
                  </div>
                </html>
              }
            }
          } ~
          path("headers" ) { // This shows all headers sent with the request
            getAllHeaders {httpHeaders =>
              complete {
                <html>
                  <head>
                    <meta charset="UTF-8" />
                    <title>Web2Txt</title>
                    <link rel="stylesheet" href="/css/normalize.css" />
                    <link rel="stylesheet" href="/css/skeleton.css" />
                  </head>
                  <div class="container">
                    <h1>HTTP Headers</h1>
                    <p>${httpHeaders.toString()}</p>
                  </div>
                </html>
              }
            }
          } ~
          path("set-cookie" ) {
            dynamic {
              /**  This is meant to work like the website of a popular newspaper
                *  It sets a cookie and redirects to a URI that checks whether the cookie was set
                *  If it is, the user is redirected to the original destination, setting an additional cookie
                *  If the cookie is not set, Varnish redirects to a "cookies required" URI
                */
              setCookie(genCookie0()){
                redirect(
                  Uri("/require-cookie"), // URI where it is verified that the cookie has been set
                  StatusCodes.SeeOther
                )
              }
            }
          }
        }
      }
    }
  }

}