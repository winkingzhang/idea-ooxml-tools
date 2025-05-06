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

import com.intellij.ide.projectView.SelectableTreeStructureProvider
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import org.zhangwenqing.ideaooxml.fileTypes.DocxFileType
import org.zhangwenqing.ideaooxml.fileTypes.PptxFileType
import org.zhangwenqing.ideaooxml.fileTypes.XlsxFileType
import org.zhangwenqing.ideaooxml.nodes.OoxmlPsiNode
import org.zhangwenqing.ideaooxml.utils.FsUtil
import org.zhangwenqing.ideaooxml.vfs.OoxmlFileSystem
import org.zhangwenqing.ideaooxml.vfs.impl.DocxFileSystemImpl
import org.zhangwenqing.ideaooxml.vfs.impl.PptxFileSystemImpl
import org.zhangwenqing.ideaooxml.vfs.impl.XlsxFileSystemImpl
import java.nio.file.Files

class OoxmlTreeProvider : TreeStructureProvider, SelectableTreeStructureProvider, DumbAware {
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings
    ): Collection<AbstractTreeNode<*>> {
        return children.map {
            if (it is PsiFileNode && it.virtualFile?.isValid == true) {
                val project = parent.project
                var treeNodeFile = it.virtualFile ?: return@map it

                var isNestedFile = false

                // copy the nested file into temporary folder if it should be.
                if (OoxmlFileSystem.isNestedFile(treeNodeFile.path)) {
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
                    DocxFileType -> DocxFileSystemImpl.Util.getInstance().getRootByLocal(treeNodeFile)
                    PptxFileType -> PptxFileSystemImpl.Util.getInstance().getRootByLocal(treeNodeFile)
                    XlsxFileType -> XlsxFileSystemImpl.Util.getInstance().getRootByLocal(treeNodeFile)
                    else -> null
                }

                if (archiveFile != null) {
                    return@map OoxmlPsiNode(project, it.value, settings, isNestedFile, archiveFile)
                }
            }

            return@map it
        }
    }

    override fun getTopLevelElement(element: PsiElement?): PsiElement? {
        element?.let { PsiUtilCore.ensureValid(it) }
        val containingFileRef = Ref.create<PsiFile?>()
        ApplicationManager.getApplication().runReadAction {
            containingFileRef.set(element?.containingFile)
        }

        val containingFile = containingFileRef.get()
        if (!(containingFile.fileType == DocxFileType || containingFile.fileType == PptxFileType || containingFile.fileType == XlsxFileType)) {
            return null
        }
        return containingFile
    }
}