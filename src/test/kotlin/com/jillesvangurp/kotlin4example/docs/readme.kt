package com.jillesvangurp.kotlin4example.docs

import com.jillesvangurp.kotlin4example.DocGenTest
import com.jillesvangurp.kotlin4example.SourceRepository

val k4ERepo = SourceRepository("https://github.com/jillesvangurp/kotlin4example", branch = "master")

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
            +"""
                The block you pass to example can be a suspending block. It uses `runBlocking` to run it. Earlier
                versions of this library had a separate function for this; this is no longer needed.
            """.trimIndent()
        }

        subSection("Configuring examples") {

            +"""
                Sometimes you just want to show but not run the code. You can control this with the 
                `runExample` parameter.
            """.trimIndent()
            example(runExample = false) {
                //
                example(
                    runExample = false,
                ) {
                    // your code goes here
                }
            }
            +"""
                The library imposes a line length of 80 characters on your examples. The 
                reason is that code blocks with horizontal scroll bars look ugly. 
                
                You can of course turn this off or turn on the built in wrapping (wraps at the 80th character) 
                
            """.trimIndent()
            example(runExample = false) {

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
                    // your code goes here
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
                // the BEGIN_ and END_ are optional but I find it
                // helps for readability.
                // BEGIN_MY_CODE_SNIPPET
                println("Example code that shows in a snippet")
                // END_MY_CODE_SNIPPET
                exampleFromSnippet("readme.kt", "MY_CODE_SNIPPET")
            }
        }
        subSection("Markdown") {
            // you can use our Kotlin DSL to structure your documentation.

            example(runExample = false) {
                section("Section") {
                    +"""
                        You can use string literals, templates ${1 + 1}, 
                        and [links](https://github.com/jillesvangurp/kotlin4example)
                        or other markdown formatting.
                    """.trimIndent()
                }
                // you can also just include markdown files
                // useful if you have a lot of markdown
                // content without code examples
                includeMdFile("intro.md")
                // link to things in your git repository
                mdLink(DocGenTest::class)
                mdLinkToRepoResource("build file", "build.gradle.kts")
                mdLinkToSelf("This class")
            }
        }
        subSection("Source code blocks") {
            +"""
                You can add your own source code blocks as well.
            """.trimIndent()
            example(runExample = false) {
                mdCodeBlock(
                    code = """
                        Useful if you have some non kotlin code that you want to show
                    """.trimIndent(),
                    type = "text"
                )
            }
        }
        subSection("This README is generated") {
            +"""
                This README.md is of course created from kotlin code that 
                runs as part of the test suite. You can look at the kotlin 
                source code that generates this markdown ${mdLinkToSelf("here")}.
    
                The code that writes the `README.md file` is as follows:
            """.trimIndent()
            exampleFromSnippet(DocGenTest::class, "READMEWRITE")
        }
        subSection("Context receivers") {

            +"""
                A new feature in Kotlin that you currently have to opt into is context receivers.
                
                Context receivers are useful for processing the output of your examples since you typically
                need Kotlin4Example when you use the ExampleOutput.
                
                I don't want
                to force people to opt into context receivers yet but it's easy to add this yourself.
                
                Simply add a simple extension function like this:.
            """.trimIndent()

            mdCodeBlock("""
                context(Kotlin4Example)!
                fun ExampleOutput<*>.printStdOut() {
                  +""${'"'}
                    This prints:
                  ""${'"'}.trimIndent()
                 
                  mdCodeBlock(stdOut, type = "text", wrap = true)
                }
            """.trimIndent(), "kotlin")

            +"""
                And then you can use it `example { 1+1}.printStdOut()`.
                
                To opt into context receivers, add this to your build file
            """.trimIndent()
            mdCodeBlock("""
                kotlin {
                    compilerOptions {
                        freeCompilerArgs= listOf("-Xcontext-receivers")
                    }
                }                
            """.trimIndent(), "kotlin")

        }
    }

    includeMdFile("outro.md")
}



