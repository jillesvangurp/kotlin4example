package com.jillesvangurp.kotlin4example

import com.jillesvangurp.kotlin4example.docs.readme
import org.junit.jupiter.api.Test

class DocGenTest {
    // READMEWRITEBEGIN
    @Test
    fun `generate readme for this project`() {
        val readmeMd = Page("Kotlin4Example",fileName = "README.md")
        readmeMd.write(markdown = readme)
    }
    // READMEWRITEEND
}
