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

package org.zhangwenqing.ideaooxml.nodes

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiDirectoryImpl

class OoxmlPsiNode(project: Project,
                   value: PsiFile,
                   viewSettings: ViewSettings,
                   private val isNestedFile: Boolean,
                   private val archiveFile: VirtualFile
) : PsiFileNode(project, value, viewSettings) {
    override fun getChildrenImpl(): Collection<AbstractTreeNode<*>> {
        val psiManager = PsiManager.getInstance(project)
        val psiDir: PsiDirectory? = if (isNestedFile) {
            PsiDirectoryImpl(psiManager as PsiManagerImpl, archiveFile)
        } else {
            psiManager.findDirectory(archiveFile)
        }

        val list = mutableListOf<AbstractTreeNode<*>>()
        psiDir?.let {
            list.addAll(ZipBasedPsiNode(project, it, settings, archiveFile).children)
        }
        return list
    }
}