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

package org.zhangwenqing.ideaooxml.extensions

import com.intellij.ide.highlighter.ArchiveFileType
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.zhangwenqing.ideaooxml.filesystem.ZipBasedFileSystem
import org.zhangwenqing.ideaooxml.filesystem.impl.ZipFileSystemImpl
import org.zhangwenqing.ideaooxml.nodes.OoxmlPsiNode
import org.zhangwenqing.ideaooxml.utils.FsUtil
import java.nio.file.Files

class OoxmlTreeProvider : TreeStructureProvider {
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings
    ): Collection<AbstractTreeNode<*>> {
        return children.map { it ->
            if (it is PsiFileNode && it.virtualFile?.isValid == true) {
                val project = parent.project
                var treeNodeFile = it.virtualFile ?: return@map it

                var isNestedFile = false

                // copy the nested file into temporary folder if it should be.
                if (ZipBasedFileSystem.isNestedFile(treeNodeFile.path)) {
                    val targetFile = FsUtil.getTempFilePath(treeNodeFile).toFile()
                    if (!targetFile.exists()) {
                        targetFile.parentFile.mkdirs()
                        treeNodeFile.inputStream.use { input ->
                            Files.copy(input, targetFile.toPath())
                        }
                    }
                    val targetVf = LocalFileSystem.getInstance().findFileByIoFile(targetFile)
                    if (targetVf != null) {
                        treeNodeFile = targetVf
                        isNestedFile = true
                    }
                }

                val archiveFile: VirtualFile? = when (treeNodeFile.fileType) {
                    ArchiveFileType.INSTANCE ->
                        ZipFileSystemImpl.getInstance().getRootByLocal(treeNodeFile)

//                    DocxFileType -> DocxFileSystem.getInstance().getRootByLocal(treeNodeFile)
//                    PptxFileType -> PptxFileSystem.getInstance().getRootByLocal(treeNodeFile)
//                    XlsxFileType -> XlsxFileSystem.getInstance().getRootByLocal(treeNodeFile)
                    else -> null
                }

                if (archiveFile != null) {
                    return@map OoxmlPsiNode(project, it.value, settings, isNestedFile, archiveFile)
                }
            }

            return@map it
        }
    }
}