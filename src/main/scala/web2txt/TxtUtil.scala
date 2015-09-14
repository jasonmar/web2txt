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

import java.nio.CharBuffer

import scala.annotation.tailrec

object TxtUtil {

  def skipTo(chars: Array[Char], buf: CharBuffer): Unit = {
    var i = 0
    val n = chars.length
    if (n > 1) {
      while (i < n && buf.hasRemaining) {
        if (buf.get(buf.position) != chars(0)) {
          skipTo(chars(0), buf)
          i = 1
        }
        while (i < n && buf.hasRemaining && buf.get() == chars(i)) {
          i += 1
        }
      }
    } else if (n != 0) {
      skipTo(chars(0),buf)
    }
  }

  def skipTo(s: String, buf:CharBuffer): Unit = skipTo(s.toCharArray,buf)

  @tailrec
  def skipTo(c: Char, buf: CharBuffer): Unit = {
    if (buf.hasRemaining && buf.get() != c) {
      skipTo(c,buf)
    }
  }
  
  val href0 = Array('h','r','e','f','=','"')
  val src0 = Array('s','r','c','=','"')
  val a0 = Array('a',' ')
  val scr0 = Array('s','c','r','i','p','t')
  val scr1 = Array('<','/','s','c','r','i','p','t','>')
  val style0 = Array('s','t','y','l','e')
  val style1 = Array('<','/','s','t','y','l','e','>')
  val img0 = Array('i','m','g')
  val rel0 = Array('/')
  val link0 = Array('/','/')
  val link1 = Array('h','t','t','p',':')
  val link2 = Array('h','t','t','p','s',':')

  def readFieldContents(src: CharBuffer, dest: CharBuffer, host: Array[Char]): Unit = {
    if (!lookAhead(src,link0) && !lookAhead(src,link1) && !lookAhead(src,link2)) {
      dest.put(host)
      if (!lookAhead(src,rel0)) dest.put('/')
    }
    var value = src.get()
    while (value != '"' && src.hasRemaining) {
      dest.put(value)
      value = src.get()
    }
    if (value != '>') skipTo('>',src)
  }

  def readTagContents(src: CharBuffer, dest: CharBuffer, host: Array[Char]): Unit = {
    skipTo('>',src)
    if (src.position > 1 && src.get(src.position - 1) != '/') {
      var value: Char = 0
      while (src.remaining > 3 && value != '>') {
        value = src.get()
        if (value != '<') {
          dest.put(value)
        } else if (src.get(src.position) != '/') {
          if (lookAhead(src,a0)) { // capture href="*"
            dest.put('\n') // begin link on a new line
            skipTo(href0,src)
            readFieldContents(src,dest,host)
            dest.put(' ')
          } else if (lookAhead(src,img0)) { // capture img src="*"
            skipTo(src0,src)
            readFieldContents(src,dest,host)
            dest.put(' ')
          } else if (lookAhead(src,scr0)) { // ignore script>*</script>
            skipTo(scr1,src)
          } else if (lookAhead(src,style0)) { // ignore style>*</style>
            skipTo(style1,src)
          } else { // read the entire contents of the tag
            readTagContents(src, dest, host)
          }
        } else {
          skipTo('>',src)
          value = '>'
        }
      }
    }

  }
  
  def lookAhead (b: CharBuffer, a: Array[Char]): Boolean = {
    if (b.remaining >= a.length) {
      var i = 0
      while (i < a.length && b.get(b.position + i) == a(i)) {
        i += 1
      }
      if (i == a.length) true
      else false
    } else {
      false
    }
  }
}
