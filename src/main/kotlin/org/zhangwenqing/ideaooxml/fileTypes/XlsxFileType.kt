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

package org.zhangwenqing.ideaooxml.fileTypes

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls
import org.zhangwenqing.ideaooxml.utils.IconList
import javax.swing.Icon

object XlsxFileType : FileType {
    override fun getName(): @NonNls String  = "XLSX"

    override fun getDescription(): @NlsContexts.Label String = "Microsoft Excel Spreadsheet"

    @Suppress("UnstableApiUsage")
    override fun getDefaultExtension(): @NlsSafe String = "xlsx"

    override fun getIcon(): Icon = IconList.Xlsx

    override fun isBinary(): Boolean = true

//    override fun isReadOnly(): Boolean = true
}