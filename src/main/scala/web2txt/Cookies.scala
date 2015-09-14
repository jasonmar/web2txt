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

import spray.http._

object Cookies {

  def getResponseCookies(r: HttpResponse): Option[Seq[HttpCookie]] = {
    r.headers.filter(_.is("set-cookie"))
      .map(_.asInstanceOf[HttpHeaders.`Set-Cookie`].cookie)
      .toSeq match {
      case x if x.nonEmpty => Some(x)
      case _ => None
    }
  }

  def getRequestCookies(r: HttpRequest): Option[Seq[HttpCookie]] = {
    r.header[HttpHeaders.Cookie].map{_.cookies}
  }
  
  def mergeCookies(r0: HttpRequest, r1: HttpResponse): Option[Seq[HttpCookie]] = {
    val r0c = getRequestCookies(r0)
    val r1c = getResponseCookies(r1)
    (r0c,r1c) match {
      case (None,Some(y)) => 
        r1c
      case (Some(x),None) =>
        r0c
      case (Some(x),Some(y)) =>
        Some(joinCookies(x,y))
      case _ => 
        None
    }
    
  }
  
  def joinCookies(x: Seq[HttpCookie], y: Seq[HttpCookie]): Seq[HttpCookie] = {
    x.union(y).distinct
  }

  def getCookies(headers: List[HttpHeader]): Option[Seq[HttpCookie]] = {
    headers.filter(_.is("cookie"))
      .map{_.asInstanceOf[HttpHeaders.Cookie]}
      .flatMap{_.cookies}.toSeq match {

      case x if x.nonEmpty =>
        Some(x)

      case _ =>
        None
    }
  }
  
  def addCookies(headers: List[HttpHeader], addCookies: Option[Seq[HttpCookie]]): List[HttpHeader] = {
    addCookies match {
      case Some(moreCookies) if moreCookies.nonEmpty =>
        getCookies(headers) match {
          case Some(cookies) =>
            headers.filterNot(_.is("cookie")) :+ HttpHeaders.Cookie(joinCookies(cookies,moreCookies))
          case _ =>
            headers :+ HttpHeaders.Cookie(moreCookies)
        }
      case _ =>
        headers
    }
  }

  def futureDateTime(n: Int): DateTime = {
    // n Days * 24 hours/day * 60 minutes/hour * 60 seconds/minute * 1000 milliseconds/second
    DateTime(System.currentTimeMillis() + (n * 24L * 3600L * 1000L))
  }

  val cookie0Name = "C0"
  val cookie1Name = "C1"

  def genCookie0(): HttpCookie = {
    HttpCookie(
      name = cookie0Name,
      content = "007f010c259c55f3cef60006", //TODO generate this dynamically
      expires = Some(futureDateTime(31)),
      path = Some("/")
    )
  }

  def genCookie1(): HttpCookie = {
    HttpCookie(
      name = cookie1Name,
      content = "0M6DbMwq2o5ZnDXrmvxADcHG41fPb6klY9deFz9JchiAIUFL2BEX5FWcV.Ynx4rkFI", //TODO generate this dynamically
      expires = Some(futureDateTime(31)),
      path = Some("/")
    )
  }

  def isValid(c: HttpCookie): Boolean = {
    // TODO implement a real check based on signature validation or similar
    if (c.name == cookie0Name && c.content.nonEmpty) true
    else false
  }

}
