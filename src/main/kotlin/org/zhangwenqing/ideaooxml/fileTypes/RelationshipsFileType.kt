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

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.XmlLikeFileType
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object RelationshipsFileType : XmlLikeFileType(XMLLanguage.INSTANCE) {
    override fun getName(): @NonNls String = "Relationships"

    override fun getDescription(): @NlsContexts.Label String = "XML"

    override fun getDefaultExtension(): @NlsSafe String = "rels"

    override fun getIcon(): Icon {
        return AllIcons.FileTypes.Xml
    }
}