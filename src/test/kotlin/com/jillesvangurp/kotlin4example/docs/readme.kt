package com.jillesvangurp.kotlin4example.docs

import com.jillesvangurp.kotlin4example.DocGenTest
import com.jillesvangurp.kotlin4example.SourceRepository

val k4ERepo = SourceRepository("https://github.com/jillesvangurp/kotlin4example")

// READMESTART
val readme by k4ERepo.md {
    // for larger bits of text, it's nice to load them from a markdown file
    includeMdFile("intro.md")

    block(runBlock = false) {
        // documentation inception
        // this is technically a block within a block, just so I can show you
        // how you would use it.
        block {
            println("Hello World")
        }
    }
    // but of course you can inline a Kotlin multiline string with some markdown
    +"""
        Here's the same block as above running as part of this 
        ${mdLinkToSelf("readme.kt")} file.
    """

    block {
        println("Hello World")
    }

    +"""
        As you can see, we indeed show a pretty printed block, ran it, and
        grabbed the output. Observant readers will also note that the nested 
        block above did not run. The reason for this is that the outer `block` 
        call for that has a parameter that you can use to prevent this. 
        If you look at the source code for the readme, you will see we used 
        `block(runBlock = false)`
        
        We can also return a value from the block and capture that:
    """

    block {
        fun aFunctionThatReturnsAnInt() = 1+1

        // call the function to make the block return something
        aFunctionThatReturnsAnInt()
    }

    +"""
        Note how that captured the return value and printed that 
        without us using `print` or `println`.
    """

    +"""
        ## This README is generated
        
        This README.md is actually created from kotlin code that 
        runs as part of the test suite. You can look at the kotlin 
        source code that generates this markdown ${mdLinkToSelf("here")}.
    """.trimIndent()

    snippetFromSourceFile("com/jillesvangurp/kotlin4example/docs/readme.kt","README")

    """
        And the code that actually writes the file is a test:
    """.trimIndent()
    snippetBlockFromClass(DocGenTest::class, "READMEWRITE")

    includeMdFile("outro.md")
}
// READMEEND
