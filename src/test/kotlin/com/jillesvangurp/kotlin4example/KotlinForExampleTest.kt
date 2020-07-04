package com.jillesvangurp.kotlin4example

import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

val repo = SourceRepository("https://github.com/jillesvangurp/kotlin4example")

val testDocOutsideClass by repo.md {
    // should not contain FooBar
    blockWithOutput {
        // should contain BarFoo from this comment
        println("Hello" + " World!")
    }
    +"${mdLinkToSelf()}"
    // and the output of the println
}

class KotlinForExampleTest {
    @Test
    fun `should render markdown with code block outside class`() {
        testDocOutsideClass shouldNotContain "FooBar"
        testDocOutsideClass shouldContain "BarFoo"
        testDocOutsideClass shouldContain "Hello World!" // it should have captured the output of println this
    }

    @Test
    fun `link to self should be correct`() {
        testDocOutsideClass shouldContain "https://github.com/jillesvangurp/kotlin4example/tree/master/src/test/kotlin/com/jillesvangurp/kotlin4example/KotlinForExampleTest.kt"
    }

    @Test
    fun `do not allow long source lines`() {
        assertThrows<IllegalArgumentException> {
            repo.md {
                block {
                    // too long
                    println("****************************************************************************************************")
                }
            }.value // make sure to access the value
        }
    }

    @Test
    fun `wrap long source lines`() {
        repo.md {
            block(wrap = true) {
                // too long but will be wrapped
                println("****************************************************************************************************")
            }
        }.value.lines().forEach {
            it.length shouldBeLessThanOrEqual 80
        } // make sure to access the value
    }

    @Test
    fun `Example markdown`() {
        val markdown = repo.md {
            // we can inject arbitrary markdown, up to you

            +"""
                ## We can put some markdown in a string and include that

                Some text with .
                with multiple
                   lines
                   and some
                        indentation
                that we will try to trim responsibly.
            """

            +"""
                ## Block with a return value

                Here comes the magic. We find the source file, extract the block, and include that as a
                markdown code block.

                We also capture the return value of the block and print it.
            """
            blockWithOutput {
                val aNumber = Random.nextInt(10)
                if (aNumber > 10) {
                    println("That would be unexpected")
                } else {
                    // block's return value is captured and printed
                    println("Wow random works! We got: $aNumber")
                }
            }

            +"""
                ## Here's a block that returns unit

                Not all examples have to return a value.
            """

            block {
                // Unit is the return value of this block, don't print that
            }

            +"""
                ## We can also do blocks that take a buffered writer

                Buffered writers have a print and println method.
                So you can pretend to print to stdout and we capture the output

            """
            blockWithOutput {
                // Obviously ...
                println("Hello world")
            }
        }
        // what you do with the markdown is your problem. I'd suggest writing it to a file.

        println(markdown)
    }
}
