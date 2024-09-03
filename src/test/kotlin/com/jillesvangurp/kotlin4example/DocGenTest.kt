package com.jillesvangurp.kotlin4example

import com.jillesvangurp.kotlin4example.docs.readmeMarkdown
import org.junit.jupiter.api.Test

// READMEWRITEBEGIN
/**
 * The readme is generated when the tests run.
 */
class DocGenTest {
    @Test
    fun `generate readme for this project`() {
        val readmePage = Page(
            title = "Kotlin4Example",
            fileName = "README.md"
        )
        // readmeMarkdown is a lazy of the markdown content
        readmePage.write(markdown = readmeMarkdown)
    }
}
// READMEWRITEEND
