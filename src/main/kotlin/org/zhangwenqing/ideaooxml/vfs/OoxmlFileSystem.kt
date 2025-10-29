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

package org.zhangwenqing.ideaooxml.vfs

import com.intellij.ide.highlighter.ArchiveFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VFileProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFilePointerCapableFileSystem
import com.intellij.openapi.vfs.impl.ZipHandlerBase
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem
import com.intellij.openapi.vfs.newvfs.ManagingFS
import com.intellij.openapi.vfs.newvfs.NewVirtualFile
import com.intellij.openapi.vfs.newvfs.VfsImplUtil
import com.intellij.openapi.vfs.newvfs.VfsImplUtil.PathFromRoot
import org.apache.commons.lang3.StringUtils
import org.zhangwenqing.ideaooxml.vfs.zip.OoxmlZipHandler


abstract class OoxmlFileSystem : ArchiveFileSystem(), VirtualFilePointerCapableFileSystem {
    companion object {
        const val SEPARATOR = "!/"

        val FILE_SEPARATORS = SEPARATOR + (if (java.io.File.separatorChar == '/') "" else java.io.File.separator)

        @JvmStatic
        fun isPathValid(path: String): Boolean {
            return path.contains(SEPARATOR)
        }

        @JvmStatic
        fun containExtension(path: String): Boolean {
            val typeManager = FileTypeManager.getInstance()
            val matchers = typeManager.getAssociations(
                ArchiveFileType.INSTANCE
            )
            return matchers.any {
                it.acceptsCharSequence(path)
            }
        }

        @JvmStatic
        fun isNestedFile(path: String): Boolean {
            return if (StringUtils.countMatches(path, SEPARATOR) > 0) containExtension(path) else false
        }

        @JvmStatic
        fun normalizeImp(path: String): String {
            val separatorIndex = path.indexOf(SEPARATOR)
            return if (separatorIndex > 0) FileUtil.normalize(path.take(separatorIndex)) + path.substring(separatorIndex)
            else path
        }

        @JvmStatic
        fun extractRootPathImp(normalizedPath: String): String {
            val separatorIndex = normalizedPath.indexOf(SEPARATOR)
            return if (separatorIndex > 0) normalizedPath.take(separatorIndex + SEPARATOR.length) else ""
        }
    }

    abstract fun isPreferredFileType(fileType: FileType): Boolean

    override fun isCorrectFileType(local: VirtualFile): Boolean {
        // detect local file type is zip compatible or not
        val fileType: FileType = FileTypeRegistry.getInstance().getFileTypeByFileName(local.name)
        return this.isPreferredFileType(fileType)
    }

    override fun extractPresentableUrl(path: String): String =
        super.extractPresentableUrl(StringUtil.trimEnd(path, SEPARATOR))

    override fun normalize(path: String): String = normalizeImp(path)

    override fun extractRootPath(normalizedPath: String): String = extractRootPathImp(normalizedPath)

    override fun extractLocalPath(rootPath: String): String = StringUtil.trimEnd(rootPath, SEPARATOR)

    override fun composeRootPath(localPath: String): String = localPath + SEPARATOR

    override fun getHandler(entryFile: VirtualFile): ZipHandlerBase =
        // use default zip handler would be enough
        VfsImplUtil.getHandler(this, entryFile) { path -> OoxmlZipHandler(path) }

    override fun findFileByPath(path: String): VirtualFile? =
        if (isPathValid(path)) findFileByTraversing(path, NewVirtualFile::findChild) else null

    override fun findFileByPathIfCached(path: String): VirtualFile? =
        if (isPathValid(path)) findCachedFileByPath(path) else null

    override fun refreshAndFindFileByPath(path: String): VirtualFile? =
        if (isPathValid(path)) VfsImplUtil.refreshAndFindFileByPath(this, path) else null

    override fun refresh(asynchronous: Boolean) {
        VfsImplUtil.refresh(this, asynchronous)
    }

    private fun findCachedFileByPath(path: String): NewVirtualFile? {
        return findFileByTraversing(
            path, NewVirtualFile::findChildIfCached, {
                val canonicalPath = it?.canonicalPath
                val canonicalFile = if (canonicalPath != null) findCachedFileByPath(canonicalPath) else null
                canonicalFile?.parent
            })
    }

    private fun findFileByTraversing(
        path: String,
        findItem: NewVirtualFile.(String) -> NewVirtualFile?,
        traveParentSymlink: (NewVirtualFile?) -> NewVirtualFile? = { it?.canonicalFile?.parent },
        traveParent: (NewVirtualFile?) -> NewVirtualFile? = { it?.parent }
    ): NewVirtualFile? {
        val pair = extractRootFromPath(path) ?: return null
        val parts = StringUtil.tokenize(pair.pathFromRoot, FILE_SEPARATORS)
        var file: NewVirtualFile? = pair.root

        for (pathElement in parts) {
            if (pathElement?.isEmpty()!! || "." == pathElement) continue

            file = if (".." == pathElement) {
                if (file?.`is`(VFileProperty.SYMLINK) == true) {
                    traveParentSymlink(file)
                } else {
                    traveParent(file)
                }
            } else {
                file?.findItem(pathElement)
            }

            if (file == null) {
                return null
            }
        }

        return file
    }

    private fun extractRootFromPath(path: String): PathFromRoot? {
        val normalizedPath = normalizeImp(path)
        if (normalizedPath.isBlank()) {
            return null
        }

        val rootPath = extractRootPathImp(normalizedPath)
        if (rootPath.isBlank() || rootPath.length > normalizedPath.length) {
            return null
        }

        val root = ManagingFS.getInstance().findRoot(rootPath, this)
        if (root == null || !root.exists()) {
            return null
        }

        var restPathStart = rootPath.length
        if (restPathStart < normalizedPath.length && normalizedPath[restPathStart] == '/') {
            ++restPathStart
        }

        return PathFromRoot(root, normalizedPath.substring(restPathStart))
    }
}