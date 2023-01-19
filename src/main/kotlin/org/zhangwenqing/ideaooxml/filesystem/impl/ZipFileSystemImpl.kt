package org.zhangwenqing.ideaooxml.filesystem.impl

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import org.zhangwenqing.ideaooxml.filesystem.ZipBasedFileSystem

// test only
class ZipFileSystemImpl: ZipBasedFileSystem() {
    companion object {
        const val PROTOCOL = "zip"

        fun getInstance(): ZipFileSystemImpl {
            return VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as ZipFileSystemImpl
        }
    }

    override fun isCorrectFileType(local: VirtualFile): Boolean {
        return true
    }

    override fun getProtocol(): String = PROTOCOL
}