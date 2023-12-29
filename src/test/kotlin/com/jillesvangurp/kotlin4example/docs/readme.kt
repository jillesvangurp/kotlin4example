package com.jillesvangurp.kotlin4example.docs

import com.jillesvangurp.kotlin4example.DocGenTest
import com.jillesvangurp.kotlin4example.SourceRepository

val k4ERepo = SourceRepository("https://github.com/jillesvangurp/kotlin4example")

// You can use comment markers to grab larger sections of code
// or to grab code from e.g. the main source tree.
// READMECODESTART
val readmeMarkdown by k4ERepo.md {
    // for larger bits of text, it's nice to load them from a markdown file
    includeMdFile("intro.md")

    section("Usage") {
        subSection("Example blocks") {
            +"""
                With Kotlin4Example you can mix examples and markdown easily. 
                An example is a code block
                and it is executed by default. Because it is a code block,
                 you are forced to ensure
                it is syntactically correct and compiles. 
                
                By executing it, you can further guarantee it does what it 
                is supposed to and you can
                intercept output and integrate that into your documentation.
                
                For example:
            """.trimIndent()

            // a bit of kotlin4example inception here, but it works
example {
    // out is an ExampleOutput instance
    // with both stdout and the return
    // value as a Result<T>. Any exceptions
    // are captured as well.
    val out = example {
        print("Hello World")
    }
// this is how you can append arbitrary markdown
+"""
    This example prints **${out.stdOut}** when it executes. 
""".trimIndent()
            }
        }
        subSection("Suspending examples") {
            +"If you use co-routines, you can use a suspendingExample"

            example {
                // runs the example in a runBlocking { .. }
                suspendingExample {
                    // call some suspending code
                }
            }
        }
        subSection("Configuring blocks") {

            example(runExample = false) {
                // sometimes you just want to show but not run the code
                example(
                    runExample = false,
                ) {
                    // your example goes here
                }

                // making sure the example fits in a web page
                // long lines tend to look ugly in documentation
                example(
                    // default is 80
                    lineLength = 120,
                    // default is false
                    wrap = true,
                    // default is false
                    allowLongLines = true,

                ) {
                    // more code here
                }
            }
        }
        subSection("Code snippets") {
            +"""
                While it is nice to have executable blocks, 
                sometimes you just want to grab
                code directly from a file. You can do that with snippets.
            """.trimIndent()

            example {
                // BEGIN_MY_CODE_SNIPPET
                println("Example code")
                // END_MY_CODE_SNIPPET
                exampleFromSnippet("readme.kt","MY_CODE_SNIPPET")
            }
        }
        subSection("Markdown") {
            // you can use our Kotlin DSL to structure your documentation.
            example(runExample = false) {
                section("Section") {
                    +"""
                        You can use string literals, templates ${1+1}, 
                        and [links](https://github.com/jillesvangurp/kotlin4example)
                        or other markdown formatting.
                    """.trimIndent()
                }
                // you can also just include markdown files
                includeMdFile("intro.md")
                // link to things in your git repository
                mdLink(DocGenTest::class)
                mdLinkToRepoResource("build file","build.gradle.kts")
            }
        }
    }

    section("This README is generated") {
        +"""
            This README.md is of course created from kotlin code that 
            runs as part of the test suite. You can look at the kotlin 
            source code that generates this markdown ${mdLinkToSelf("here")}.
        """.trimIndent()

        // little string concatenation hack so it will read 
        // until the end marker instead of stopping here        
        exampleFromSnippet(
            "com/jillesvangurp/kotlin4example/docs/readme.kt",
            "README" + "CODE"
        )

        """
            And the code that actually writes the `README.md file` is a test:
        """.trimIndent()
        exampleFromSnippet(DocGenTest::class, "READMEWRITE")
    }

    includeMdFile("outro.md")
}
// READMECODEEND


