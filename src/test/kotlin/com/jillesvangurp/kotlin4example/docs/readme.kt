package com.jillesvangurp.kotlin4example.docs

import com.jillesvangurp.kotlin4example.DocGenTest
import com.jillesvangurp.kotlin4example.Page
import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.kotlin4example.mdPageLink

// BEGIN_REPO_DEFINITION
val k4ERepo = SourceRepository(
    // used to construct markdown links to files in your repository
    repoUrl = "https://github.com/jillesvangurp/kotlin4example",
    // default is main
    branch = "master",
    // this is the default
    sourcePaths = setOf(
        "src/main/kotlin",
        "src/test/kotlin"
    )
)
// END_REPO_DEFINITION

val readmeMarkdown by k4ERepo.md {
    // for larger bits of text, it's nice to just load them from a markdown file
    includeMdFile("intro.md")

    // structure your markdown sections with kotlin nested blocks
    section("Getting Started") {
        +"""
            After adding this library to your (test) dependencies, you can start adding code 
            to generate markdown. 
        """.trimIndent()

        subSection("Creating a SourceRepository") {

            +"""
                The first thing you need is a `SourceRepository` definition. This is needed to tell
                kotlin4example about your repository, where to link, and where your code is.
                
                Some of the functions in kotlin4example construct links to files in your github repository,
                or lookup code from files in your source code. 
            """.trimIndent()

            // this is how you can include code snippets from existing kotlin files
            exampleFromSnippet("com/jillesvangurp/kotlin4example/docs/readme.kt", "REPO_DEFINITION")
        }
        subSection("Creating markdown") {

            """
                Once you have a repository, you can use it to create some Markdown via an extension function:
            """.trimIndent()

            example {
                val myMarkdown = k4ERepo.md {
                    section("Introduction")
                    +"""
                        Hello world!
                    """.trimIndent()
                }
                println(myMarkdown)
            }.let {
                +"""
                    This will generate some markdown that looks as follows.
                """.trimIndent()
                mdCodeBlock(code = it.stdOut, type = "markdown")
            }
        }
        subSection("Using your Markdown to create a page") {
            +"""
                Kotlin4example has a simple page abstraction that you
                can use to organize your markdown content into pages and files
            """.trimIndent()

            val myMarkdown= "ignore"
            example(runExample = false) {
                val page = Page(title = "Hello!", fileName = "hello.md")
                // creates hello.md
                page.write(myMarkdown)
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
            +"""
                Here's a link to the source code on Github: ${mdLink(DocGenTest::class)}. 
                
                The code that constructs the markdown is a bit longer, you can find it 
                ${mdLinkToRepoResource("here", "com/jillesvangurp/kotlin4example/docs/readme.kt")}.
            """.trimIndent()
        }
    }
    section("Example blocks") {
        +"""
            With Kotlin4Example you can mix examples and markdown easily. 
            An example is a Kotlin code block. Because it is a code block,
             you are forced to ensure it is syntactically correct and that it compiles. 
            
            By executing the block (you can disable this), you can further guarantee it does what it 
            is supposed to and you can intercept output and integrate that into your 
            documentation as well
            
            For example:
        """.trimIndent()

        // a bit of kotlin4example inception here, but it works
        example(runExample = false) {
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
            The block you pass to example can be a suspending block; so you can create examples for 
            your co-routine libraries too. Kotlin4example uses `runBlocking` to run your examples.
            
            When you include the above in your Markdown it will render as follows:
        """.trimIndent()

        example {
            print("Hello World")
        }.let { out ->
            // this is how you can append arbitrary markdown
            +"""
                This example prints **${out.stdOut}** when it executes. 
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
                    // but it won't run
                }
            }
            +"""
                The library imposes a default line length of 80 characters on your examples. The 
                reason is that code blocks with long lines look ugly on web pages. E.g. Github will give 
                you a horizontal scrollbar.
                
                You can of course turn this off or turn on the built in wrapping (wraps at the 80th character) 
                
            """.trimIndent()
            example(runExample = false) {

                // making sure the example fits in a web page
                // long lines tend to look ugly in documentation
                example(
                    // use longer line length
                    // default is 80
                    lineLength = 120,
                    // wrap lines that are too long
                    // default is false
                    wrap = true,
                    // don't fail on lines that are too long
                    // default is false
                    allowLongLines = true,

                    ) {
                    // your code goes here
                }
            }
        }

        subSection("Code snippets") {
            +"""
                While it is nice to have executable blocks as examples, 
                sometimes you just want to grab
                code directly from some Kotlin file. You can do that with snippets.
            """.trimIndent()

            example {
                // BEGIN_MY_CODE_SNIPPET
                println("Example code that shows in a snippet")
                // END_MY_CODE_SNIPPET
            }
            // little hack to avoid picking up this line ;-)
            exampleFromSnippet("com/jillesvangurp/kotlin4example/docs/readme.kt","MY_" + "CODE_SNIPPET")
            +"""
                The `BEGIN_` and `END_` prefix are optional but it helps readability.
                
                You include the code in your markdown as follows:
            """.trimIndent()

            example(runExample = false) {
                exampleFromSnippet(
                    // relative path to your source file
                    // setup your source modules in the repository
                    sourceFileName = "com/jillesvangurp/kotlin4example/docs/readme.kt",
                    snippetId = "MY_CODE_SNIPPET"
                )
            }
        }
        subSection("Misc Markdown") {
            //some more features to help you write markdown

            subSubSection("Simple markdown") {
                +"""
                    This is how you can use the markdown dsl to generate markdown.
                """.trimIndent()
                example(runExample = true) {
                    section("Section") {
                        subSection("Sub Section") {
                            +"""
                                You can use string literals, templates ${1 + 1}, 
                                and [links](https://github.com/jillesvangurp/kotlin4example)
                                or other **markdown** formatting.
                            """.trimIndent()


                            subSubSection("Sub sub section") {
                                +"""
                                    There's more
                                """.trimIndent()

                                unorderedList("bullets","**bold**","*italic")

                                orderedList("one","two","three")

                                blockquote("""
                                    The difference between code and poetry ...
                                     
                                    ... poetry doesn’t need to compile.
                                    """.trimIndent()
                                )

                            }
                        }
                    }
                }
            }
            subSubSection("Links") {
                +"""
                    Linking to different things in your repository.
                """.trimIndent()
                example(runExample = false) {

                    // you can also just include markdown files
                    // useful if you have a lot of markdown
                    // content without code examples
                    includeMdFile("intro.md")

                    // link to things in your git repository
                    mdLink(DocGenTest::class)

                    // link to things in one of your source directories
                    // you can customize where it looks in SourceRepository
                    mdLinkToRepoResource(
                        title = "A file",
                        relativeUrl = "com/jillesvangurp/kotlin4example/Kotlin4Example.kt"
                    )

                    val anotherPage = Page("Page 2", "page2.md")
                    // link to another page in your manual
                    mdPageLink(anotherPage)

                    // and of course you can link to your self
                    mdLinkToSelf("This class")
                }
            }

            subSubSection("Tables") {
                +"""
                    Including tables is easy if you don't want to manually format them.
                """.trimIndent()

                example(runExample = true) {
                    table(listOf("Function","Explanation"),listOf(
                        listOf("mdLink","Add a link to a class or file in your repository"),
                        listOf("mdLinkToRepoResource","Add a file in your repository"),
                        listOf("includeMdFile","include a markdown file"),
                        listOf("example","Example code block"),
                    ))
                }
            }
        }
        subSection("Source code blocks") {
            +"""
                You can add your own source code blocks as well.
            """.trimIndent()
            example(runExample = false) {
                mdCodeBlock(
                    code = """
                        Useful if you have some **non kotlin code** that you want to show
                    """.trimIndent(),
                    type = "markdown"
                )
            }
        }
    }

    includeMdFile("outro.md")
}



