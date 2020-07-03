package com.jillesvangurp.kotlin4example

import java.io.File


data class Page(
    val title: String,
    val outputDir: String = ".",
    val fileName: String = "${title.toLowerCase().replace("""\s+""", "-")}.md"
) {
    val file = File(outputDir,fileName)

    fun write(markdown: String) {
        file.writeText("""
            # $title
            
            
            """.trimIndent() + markdown)


    }
}
