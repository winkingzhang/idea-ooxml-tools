/*
 * Copyright (c) 2025, Zhang Wenqing.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zhangwenqing.ideaooxml.vfs.zip

import com.intellij.openapi.vfs.impl.ZipHandler
import java.io.StringWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

val reformattedFileTypes = arrayOf("xml", "rels")

open class OoxmlZipHandler(path: String) : ZipHandler(path) {
    override fun contentsToByteArray(relativePath: String): ByteArray {
        val rawBytes = super.contentsToByteArray(relativePath)

        val extension = relativePath.substringAfterLast('.', "")
            .lowercase(Locale.getDefault())
        if (!reformattedFileTypes.contains(extension)) {
            return rawBytes
        }

        val rawXML = String(rawBytes)
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(rawXML.byteInputStream())
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        val result = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(result))
        return result.toString().encodeToByteArray()
    }
}