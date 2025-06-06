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

package org.zhangwenqing.ideaooxml.utils

import java.io.StringWriter
import java.util.Locale
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object XmlUtil {
    private val wellKnownXmlFileTypes = arrayOf("xml", "rels")
    private val documentBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val transformer: Transformer = TransformerFactory.newInstance().newTransformer()

    init {
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
    }

    @JvmStatic
    fun doNeedFormatXml(relativePath: String): Boolean {
        val extension = relativePath.substringAfterLast('.', "")
            .lowercase(Locale.getDefault())
        return wellKnownXmlFileTypes.contains(extension)
    }

    @JvmStatic
    fun formatXml(rawBytes: ByteArray): ByteArray {
        val rawXML = String(rawBytes)
        val document = documentBuilder.parse(rawXML.byteInputStream())
        val result = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(result))
        return result.toString().encodeToByteArray()
    }
}