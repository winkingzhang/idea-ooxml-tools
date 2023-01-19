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