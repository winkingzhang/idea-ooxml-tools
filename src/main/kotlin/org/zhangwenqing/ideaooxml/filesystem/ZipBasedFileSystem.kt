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

package org.zhangwenqing.ideaooxml.filesystem

import com.intellij.ide.highlighter.ArchiveFileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.IntegrityCheckCapableFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFilePointerCapableFileSystem
import com.intellij.openapi.vfs.impl.ZipHandler
import com.intellij.openapi.vfs.impl.ZipHandlerBase
import com.intellij.openapi.vfs.impl.jar.TimedZipHandler
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem
import com.intellij.openapi.vfs.newvfs.VfsImplUtil
import com.intellij.util.Function
import org.apache.commons.lang3.StringUtils


abstract class ZipBasedFileSystem : ArchiveFileSystem(),
    VirtualFilePointerCapableFileSystem,
    IntegrityCheckCapableFileSystem {
    companion object {
        const val SEPARATOR = "!/"

        @JvmStatic
        fun isPathValid(path: String): Boolean {
            return path.contains(SEPARATOR)
        }

        @JvmStatic
        fun containExtension(path: String): Boolean {
            val typeManager = FileTypeManager.getInstance()
            val matchers = typeManager.getAssociations(
                ArchiveFileType.INSTANCE
            ) /* + typeManager.getAssociations(
                ExtendedArchiveFileType
            ) */
            return matchers.any {
                it.acceptsCharSequence(path)
            }
        }

        @JvmStatic
        fun isNestedFile(path: String): Boolean {
            return if (StringUtils.countMatches(path, SEPARATOR) > 0) containExtension(path) else false
        }

    }

    fun getVirtualFileForZip(entryFile: VirtualFile?): VirtualFile? =
        entryFile?.let { getLocalByEntry(it) }

    fun getZipRootForLocalFile(file: VirtualFile): VirtualFile? =
        this.getRootByLocal(file)

    override fun extractPresentableUrl(path: String): String =
        super.extractPresentableUrl(StringUtil.trimEnd(path, "!/"))

    override fun normalize(path: String): String {
        val separatorIndex = path.indexOf(SEPARATOR)
        return if (separatorIndex > 0)
            FileUtil.normalize(path.substring(0, separatorIndex)) + path.substring(separatorIndex)
        else path
    }

    override fun extractRootPath(normalizedPath: String): String {
        val separatorIndex = normalizedPath.indexOf(SEPARATOR)
        require(separatorIndex >= 0) {
            "Path passed to ${this::class.java} must have separator '$SEPARATOR': $normalizedPath"
        }
        return normalizedPath.substring(0, separatorIndex + SEPARATOR.length)
    }

    override fun extractLocalPath(rootPath: String): String =
        StringUtil.trimEnd(rootPath, SEPARATOR)

    override fun composeRootPath(localPath: String): String =
        localPath + SEPARATOR

    override fun getHandler(entryFile: VirtualFile): ZipHandlerBase =
        VfsImplUtil.getHandler(
            this,
            entryFile,
            if (SystemInfo.isWindows) Function<String, ZipHandlerBase> { path: String? ->
                TimedZipHandler(
                    path!!
                )
            } else Function<String, ZipHandlerBase> { path: String? ->
                ZipHandler(
                    path!!
                )
            })

    override fun findFileByPath(path: String): VirtualFile? =
        if (isPathValid(path)) VfsImplUtil.findFileByPath(this, path) else null

    override fun findFileByPathIfCached(path: String): VirtualFile? =
        if (isPathValid(path)) VfsImplUtil.findFileByPathIfCached(this, path) else null

    override fun refreshAndFindFileByPath(path: String): VirtualFile? =
        if (isPathValid(path)) VfsImplUtil.refreshAndFindFileByPath(this, path) else null

    override fun refresh(asynchronous: Boolean) {
        VfsImplUtil.refresh(this, asynchronous)
    }

    override fun getArchiveCrcHashes(file: VirtualFile): MutableMap<String, Long> =
        this.getHandler(file).archiveCrcHashes

}