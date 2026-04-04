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

package org.zhangwenqing.office2html.excel

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.nio.charset.StandardCharsets


private const val DEFAULTS_CLASS: String = "excelDefaults"
private const val COL_HEAD_CLASS: String = "colHeader"
private const val ROW_HEAD_CLASS: String = "rowHeader"


private const val IDX_TABLE_WIDTH: Int = -2
private const val IDX_HEADER_COL_WIDTH: Int = -1

@Suppress("UNCHECKED_CAST")
private fun <K, V> mapFor(vararg mapping: Any?): MutableMap<K?, V?> {
    val map: MutableMap<K?, V?> = HashMap()
    var i: Int = 0
    while (i < mapping.size) {
        map[mapping[i] as K] = mapping[i + 1] as V?
        i += 2
    }
    return map
}

private val HALIGN: MutableMap<HorizontalAlignment?, String?> = mapFor(
    HorizontalAlignment.LEFT, "left",
    HorizontalAlignment.CENTER, "center",
    HorizontalAlignment.RIGHT, "right",
    HorizontalAlignment.FILL, "left",
    HorizontalAlignment.JUSTIFY, "left",
    HorizontalAlignment.CENTER_SELECTION, "center"
)

private val VALIGN: MutableMap<VerticalAlignment?, String?>? = mapFor(
    VerticalAlignment.BOTTOM, "bottom",
    VerticalAlignment.CENTER, "middle",
    VerticalAlignment.TOP, "top"
)

private val BORDER: MutableMap<BorderStyle?, String?>? = mapFor(
    BorderStyle.DASH_DOT, "dashed 1pt",
    BorderStyle.DASH_DOT_DOT, "dashed 1pt",
    BorderStyle.DASHED, "dashed 1pt",
    BorderStyle.DOTTED, "dotted 1pt",
    BorderStyle.DOUBLE, "double 3pt",
    BorderStyle.HAIR, "solid 1px",
    BorderStyle.MEDIUM, "solid 2pt",
    BorderStyle.MEDIUM_DASH_DOT, "dashed 2pt",
    BorderStyle.MEDIUM_DASH_DOT_DOT, "dashed 2pt",
    BorderStyle.MEDIUM_DASHED, "dashed 2pt",
    BorderStyle.NONE, "none",
    BorderStyle.SLANTED_DASH_DOT, "dashed 2pt",
    BorderStyle.THICK, "solid 3pt",
    BorderStyle.THIN, "dashed 1pt"
)

class Excel2HtmlConverter(wb1: Workbook, output1: Appendable?) {
    companion object {
        /**
         * Creates a new examples to HTML for the given workbook.
         *
         * @param wb     The workbook.
         * @param output Where the HTML output will be written.
         *
         * @return An object for converting the workbook to HTML.
         */
        fun create(wb: Workbook, output: Appendable?): Excel2HtmlConverter {
            return Excel2HtmlConverter(wb, output)
        }

        /**
         * Creates a new examples to HTML for the given workbook.  If the path ends
         * with "`.xlsx`" an [XSSFWorkbook] will be used; otherwise
         * this will use an [HSSFWorkbook].
         *
         * @param path   The file that has the workbook.
         * @param output Where the HTML output will be written.
         *
         * @return An object for converting the workbook to HTML.
         */
        @Throws(IOException::class)
        fun create(path: String, output: Appendable?): Excel2HtmlConverter {
            return create(FileInputStream(path), output)
        }

        /**
         * Creates a new examples to HTML for the given workbook.  This attempts to
         * detect whether the input is XML (so it should create an [ ] or not (so it should create an [HSSFWorkbook]).
         *
         * @param in     The input stream that has the workbook.
         * @param output Where the HTML output will be written.
         *
         * @return An object for converting the workbook to HTML.
         */
        @Throws(IOException::class)
        fun create(`in`: InputStream, output: Appendable?): Excel2HtmlConverter {
            val wb = WorkbookFactory.create(`in`)
            return create(wb, output)
        }
    }

    private fun ToHtml(wb: Workbook, output: Appendable) {
//        this.wb = wb
//        this.output = output
//        setupColorMap()
    }

    private fun setupColorMap() {
//        if (wb is HSSFWorkbook) {
//            helper = HSSFHtmlHelper(wb as HSSFWorkbook?)
//        } else if (wb is XSSFWorkbook) {
//            helper = XSSFHtmlHelper()
//        } else {
//            throw IllegalArgumentException(
//                "unknown workbook type: " + wb.getClass().getSimpleName()
//            )
//        }
    }

    /**
     * Run this class as a program
     *
     * @param args The command line arguments.
     */
    fun main(args: Array<String>) {
        if (args.size < 2) {
            System.err.println("usage: ToHtml inputWorkbook outputHtmlFile")
            return
        }

        val pw = PrintWriter(args[1], StandardCharsets.UTF_8.name())
        val toHtml = create(args[0], pw)
//        toHtml.setCompleteHTML(true)
//        toHtml.printPage()
    }
}