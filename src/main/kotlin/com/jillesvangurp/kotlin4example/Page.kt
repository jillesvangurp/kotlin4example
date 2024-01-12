package com.jillesvangurp.kotlin4example

import java.io.File

/**
 * Simple representation of a documentation page.
 */
data class Page(
    val title: String,
    val outputDir: String = ".",
    val fileName: String = "${title.lowercase().replace("""\s+""", "-")}.md"
) {
    val file = File(outputDir, fileName)

    fun write(markdown: String) {
        file.writeText(markdownContent(markdown))
    }

    fun markdownContent(markdown: String) = """
                # $title
                
                
                """.trimIndent() + markdown
}
