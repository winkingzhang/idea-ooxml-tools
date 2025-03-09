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

import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.*

object FsUtil {

    const val NESTED_FILE_TEMP_FOLDER = "ooxmlTempFiles"

    /**
     * Temporary folder to store nested archive files
     */
    @Throws(IOException::class)
    @JvmStatic
    fun getTempDirectory(): Path {
        val baseDir = File(System.getProperty("java.io.tmpdir"))
        val tempDir = File(baseDir, NESTED_FILE_TEMP_FOLDER)
        if ((tempDir.exists() && tempDir.canWrite()) || tempDir.mkdir()) {
            return tempDir.toPath()
        }
        throw IOException("Folder: ${tempDir.absolutePath} cannot be written!")
    }

    /**
     * return a temporary file path name in which is based on entry path and timestamp
     */
    @Throws(IOException::class)
    @JvmStatic
    fun getTempFilePath(nestedFile: VirtualFile): Path {
        val baseDir = getTempDirectory()
        val hash = Objects.hash(nestedFile.path, nestedFile.timeStamp)
        return baseDir.resolve("${nestedFile.name}-$hash").resolve(nestedFile.name)
    }
}