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

object Headers {

  val firefoxUserAgent = HttpHeaders.`User-Agent`("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
  val chromeUserAgent = HttpHeaders.`User-Agent`("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36")

  val defaultHeaders: List[HttpHeader] = {
    List(
      firefoxUserAgent,
      HttpHeaders.Accept(
        MediaRange(MediaTypes.`text/html`),
        MediaRange(MediaTypes.`application/xhtml+xml`),
        MediaRange(MediaTypes.`application/xml`, qValue = 0.9f),
        MediaRange.custom("*", qValue = 0.8f)
      ),
      HttpHeaders.`Accept-Language`(
        Language(primaryTag = "en-US"),
        Language(primaryTag = "en", subTags = Nil, qValue = 0.9f)
      ),
      HttpHeaders.`Accept-Encoding`(
        HttpEncodingRange(HttpEncodings.gzip),
        HttpEncodingRange(HttpEncodings.deflate)
      ),
      HttpHeaders.Connection("keep-alive"),
      HttpHeaders.`Cache-Control`(CacheDirectives.`max-age`(0L))
    )
  }

}
