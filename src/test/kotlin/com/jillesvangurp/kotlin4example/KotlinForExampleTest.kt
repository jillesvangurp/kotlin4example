package com.jillesvangurp.kotlin4example

import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

val repo = SourceRepository("https://github.com/jillesvangurp/kotlin4example")

val testDocOutsideClass by repo.md {
    // should not contain FooBar because this comment is outside the block
    block {
        // should contain BarFoo from this comment
        println("Hello" + " World!")
    }
    +mdLinkToSelf()
    // and the output of the println
}

class KotlinForExampleTest {
    @Test
    fun `should render markdown with code block outside class`() {
        testDocOutsideClass shouldNotContain "FooBar" // because in a comment outside the block
        testDocOutsideClass shouldContain "BarFoo" // from the comment
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
    fun `capture return value`() {
        repo.md {
            block {
                1+1
            }
        }.value shouldContain "2"
    }

    @Test
    fun `capture output from multiple blocks`() {
        val out1 = repo.md {
            block(printStdOut = false) {
                print("hel")
                print("lo")
            }
        }.value
        // if we disable printing nothing gets printed
        out1 shouldNotContain "hello"

        val bo = BlockOutputCapture()
        val out2 = repo.md {
            block(printStdOut = false, blockCapture = bo) {
                print("hel")
                print("lo")
            }
            block(printStdOut = true, blockCapture = bo) {
                println("world")
            }
        }.value
        // but we can reuse the same block capture and print at the end
        out2 shouldContain "hello"
        out2 shouldContain "world"
    }
}
