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

package org.zhangwenqing.ideaooxml.vfs.impl

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFileManager
import org.zhangwenqing.ideaooxml.fileTypes.XlsxFileType
import org.zhangwenqing.ideaooxml.vfs.OoxmlFileSystem

class XlsxFileSystemImpl : OoxmlFileSystem() {
    companion object {
        const val PROTOCOL = "xlsx"
    }

    object Util {
        @JvmStatic
        fun getInstance(): XlsxFileSystemImpl {
            return VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as XlsxFileSystemImpl
        }
    }

    override fun getProtocol(): String = PROTOCOL

    override fun isPreferredFileType(fileType: FileType): Boolean {
        return fileType == XlsxFileType
    }
}