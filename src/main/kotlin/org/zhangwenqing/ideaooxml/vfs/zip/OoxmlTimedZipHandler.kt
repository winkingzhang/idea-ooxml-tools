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

package org.zhangwenqing.ideaooxml.vfs.zip

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.impl.ZipHandlerBase
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.ResourceHandle
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import org.zhangwenqing.ideaooxml.utils.XmlUtil
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.ZipFile

class OoxmlTimedZipHandler(path: String) : ZipHandlerBase(path) {

    companion object {
        const val RETENTION_MS: Long = 2000L
        const val LRU_CACHE_SIZE: Int = 30

        val ourLRUCache: Object2ObjectLinkedOpenHashMap<OoxmlTimedZipHandler, ScheduledFuture<*>> =
            Object2ObjectLinkedOpenHashMap(LRU_CACHE_SIZE + 1)
        val ourScheduledExecutorService: ScheduledExecutorService =
            AppExecutorUtil.createBoundedScheduledExecutorService("Zip Handle Janitor", 1)
    }

    override fun contentsToByteArray(relativePath: String): ByteArray {
        val rawBytes = super.contentsToByteArray(relativePath)

        return if (!XmlUtil.doNeedFormatXml(relativePath)) {
            rawBytes
        } else XmlUtil.formatXml(rawBytes)
    }

    private val myHandle = ZipResourceHandle()

    override fun getEntryFileStamp(): Long = myHandle.getFileStamp()

    override fun acquireZipHandle(): ResourceHandle<ZipFile> {
        myHandle.attach()
        return myHandle
    }

    private inner class ZipResourceHandle : ResourceHandle<ZipFile>() {

        private var myFile: ZipFile? = null
        private var myFileStamp: Long = 0
        private val myLock = ReentrantLock()
        private var myInvalidationRequest: ScheduledFuture<*>? = null

        @Throws(IOException::class)
        fun attach() {
            synchronized(ourLRUCache) {
                ourLRUCache.remove(this@OoxmlTimedZipHandler)
            }
            myLock.lock()
            try {
                if (myInvalidationRequest != null) {
                    myInvalidationRequest!!.cancel(false)
                    myInvalidationRequest = null
                }
                if (myFile == null) {
                    myFileStamp = Files.getLastModifiedTime(file.toPath()).toMillis()
                    myFile = ZipFile(file)
                }
            } catch (t: Throwable) {
                myLock.unlock()
                throw t
            }
        }

        override fun close() {
            assert(myLock.isLocked)

            var invalidationRequest: ScheduledFuture<*>
            try {
                invalidationRequest =
                    ourScheduledExecutorService.schedule(
                        { invalidateZipReference(null) },
                        RETENTION_MS,
                        TimeUnit.MILLISECONDS
                    )
                myInvalidationRequest = invalidationRequest
            } finally {
                myLock.unlock()
            }

            var leastUsedHandler: OoxmlTimedZipHandler? = null
            synchronized(ourLRUCache) {
                if (ourLRUCache.putAndMoveToFirst(
                        this@OoxmlTimedZipHandler,
                        invalidationRequest
                    ) == null && ourLRUCache.size > LRU_CACHE_SIZE
                ) {
                    leastUsedHandler = ourLRUCache.lastKey()
                    invalidationRequest = ourLRUCache.removeLast()
                }
            }
            leastUsedHandler?.myHandle?.invalidateZipReference(invalidationRequest)
        }

        override fun get(): ZipFile {
            assert(myLock.isLocked)
            return myFile!!
        }

        fun getFileStamp(): Long {
            assert(myLock.isLocked)
            return myFileStamp
        }

        // `expectedInvalidationRequest` is not null when dropping out of `ourLRUCache`
        private fun invalidateZipReference(expectedInvalidationRequest: ScheduledFuture<*>?) {
            myLock.lock()
            try {
                if (myFile == null
                    || expectedInvalidationRequest != null
                    && myInvalidationRequest !== expectedInvalidationRequest
                ) {
                    return  // already closed || the handler is re-acquired
                }
                if (myInvalidationRequest != null) {
                    myInvalidationRequest!!.cancel(false)
                    myInvalidationRequest = null
                }
                try {
                    myFile!!.close()
                } catch (e: IOException) {
                    // this should not happen frequently, just grab logger when needed
                    logger<OoxmlTimedZipHandler>().info(e)
                }
                myFile = null
            } finally {
                myLock.unlock()
            }
        }
    }
}