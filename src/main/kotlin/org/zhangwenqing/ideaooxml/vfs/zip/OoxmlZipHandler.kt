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
import org.zhangwenqing.ideaooxml.utils.XmlUtil

open class OoxmlZipHandler(path: String) : ZipHandler(path) {

    override fun contentsToByteArray(relativePath: String): ByteArray {
        val rawBytes = super.contentsToByteArray(relativePath)

        return if (!XmlUtil.doNeedFormatXml(relativePath)) {
            rawBytes
        } else XmlUtil.formatXml(rawBytes)
    }
}