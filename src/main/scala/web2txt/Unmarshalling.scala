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

import java.nio.{ByteBuffer, CharBuffer}

import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.httpx.unmarshalling.Unmarshaller

import web2txt.PlainText.PlainTextResult
import web2txt.TxtUtil.{skipTo,readTagContents}

object Unmarshalling {

  def getHost(url: String): Array[Char] = {
    val i0 = url.indexOf('.')
    val i1 = url.indexOf('/',i0)
    if (i1 == -1) url.substring(0,url.length).toCharArray
    else url.substring(0,i1).toCharArray
  }

  def plainTxtUnmarshaller(url: String) = Unmarshaller[PlainTextResult](`text/xml`, `application/xml`, `text/html`, `application/xhtml+xml`, `application/pdf`) {
    case HttpEntity.NonEmpty(contentType, data) =>
      val bytes = data.toByteArray

      if (contentType.mediaType == `application/pdf`) {
        val cb = CharBuffer.allocate(bytes.length)
        PdfToTxt.pdfBytesToChars(bytes, cb)
        PlainTextResult(cb.toString)
      } else {
        val host = getHost(url)
        val cs = contentType.charset.nioCharset
        val src = cs.decode(ByteBuffer.wrap(bytes))
        val dest = CharBuffer.allocate(src.capacity())
        src.position(0)
        src.limit(src.capacity)

        while (src.hasRemaining) {
          skipTo('<',src)
          readTagContents(src, dest, host)
        }
        dest.flip()
        PlainTextResult(dest.toString)
      }

    case HttpEntity.Empty =>
      PlainTextResult("No content")
  }

  def srcUnmarshaller(url: String) = Unmarshaller[PlainTextResult](`text/xml`, `application/xml`, `text/html`, `application/xhtml+xml`) {
    case HttpEntity.NonEmpty(contentType, data) =>
      val bytes = data.toByteArray
      val cs = contentType.charset.nioCharset
      val src = cs.decode(ByteBuffer.wrap(bytes))
      PlainTextResult(src.toString)

    case HttpEntity.Empty =>
      PlainTextResult("No content")
  }

}
