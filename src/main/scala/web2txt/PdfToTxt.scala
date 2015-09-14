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

import java.io.ByteArrayInputStream
import java.nio.CharBuffer

import org.apache.pdfbox.io.RandomAccessBuffer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.PDFTextStripper

object PdfToTxt {

  def pdfBytesToChars(bytes: Array[Byte], cb: CharBuffer): CharBuffer = {
    val is = new ByteArrayInputStream(bytes)
    val buf = new RandomAccessBuffer()
    val pd = PDDocument.load(is, buf, false)
    val ts = new PDFTextStripper()
    (1 to pd.getNumberOfPages).foreach{i =>
      ts.setStartPage(i)
      ts.setEndPage(i)
      cb.put(ts.getText(pd))
    }
    cb.flip()
    cb
  }

}
