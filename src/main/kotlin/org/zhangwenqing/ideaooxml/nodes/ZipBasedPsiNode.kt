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

import com.google.common.base.MoreObjects
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager

class ZipBasedPsiNode(
    project: Project,
    value: PsiDirectory,
    viewSettings: ViewSettings,
    private val archiveFile: VirtualFile,
) : PsiDirectoryNode(project, value, viewSettings) {
    override fun getChildrenImpl(): Collection<AbstractTreeNode<*>> {
        val psiManager = PsiManager.getInstance(project)
        val virtualFiles = archiveFile.children
        val children = mutableListOf<AbstractTreeNode<*>>()

        for (file in virtualFiles) {
            if (!file.isValid) continue

            if (file.isDirectory) {
                psiManager.findDirectory(file)?.let { it ->
                    children.add(ZipBasedPsiNode(project, it, settings, file))
                }
            } else {
                psiManager.findFile(file)?.let {
                    children.add(PsiFileNode(project, it, settings))
                }
            }
        }
        return children
    }

    override fun isValid(): Boolean = true

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
            .add("archiveFile", archiveFile)
            .toString()
    }
}