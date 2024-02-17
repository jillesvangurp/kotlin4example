package com.jillesvangurp.kotlin4example

import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

val repo = SourceRepository("https://github.com/jillesvangurp/kotlin4example", branch = "master")

val testDocOutsideClass by repo.md {
    // should not contain FooBar because this comment is outside the block
    // should contain BarFoo from this comment
    renderExampleOutput(
        exampleOutput = example(true, "kotlin", false, false, 80) {
            // should contain BarFoo from this comment
            println("Hello" + " World!")
        },
        stdOutOnly = false
    )
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
                // too long
                renderExampleOutput(
                    example(true, "kotlin", false, false, 80) {
                        // too long
                        println("****************************************************************************************************")
                    },
                    false
                )

            }.value // make sure to access the value
        }
    }

    @Test
    fun `wrap long source lines`() {

        repo.md {
            // too long but will be wrapped
            renderExampleOutput(
                example(wrap = true, block = {
                    // too long but will be wrapped
                    println("****************************************************************************************************")
                }),
                false,
                wrap = true
            )

        }.value.lines().forEach {
            it.length shouldBeLessThanOrEqual 80
        } // make sure to access the value
    }

    @Test
    fun `capture return value`() {
        repo.md {
            renderExampleOutput(
                example(true, "kotlin", false, false, 80) {
                    1 + 1
                },
                false
            )
        }.value shouldContain "2"
    }

    @Test
    fun `capture return value in suspendingBlock`() {
        repo.md {
            renderExampleOutput(
                example {
                    1 + 1
                },
                false
            )
        }.value shouldContain "2"
    }

    @Test
    fun `capture output from multiple blocks`() {
        val out1 = repo.md {
            example(true, "kotlin", false, false, 80
            ) {
                print("hel")
                print("lo")
            }
        }.value
        // if we disable printing nothing gets printed
        out1 shouldNotContain "hello"

        val out2 = repo.md {
            renderExampleOutput(
                example(true, "kotlin", false, false, 80) {
                    print("hel")
                    print("lo")
                },
                true
            )
            renderExampleOutput(
                example(true, "kotlin", false, false, 80) {
                    println("world")
                },
                false
            )
        }.value
        // but we can reuse the same block capture and print at the end
        out2 shouldContain "hello"
        out2 shouldContain "world"
    }

    @Test
    fun `reindent correctly`() {
        val json = """
            {
                "foo": {
                    "bar": "foo"
                }
            }
        """.trimIndent()
        val reindented = json.reIndent(2)
        reindented.lines().firstOrNull {
            // should have cleaned up the double indent
            it.startsWith("        ")
        } shouldBe null
        reindented.lines().firstOrNull {
            // should have replaced the double indent with four spaces
            it.startsWith("    ")
        } shouldNotBe null
        reindented shouldBe """
            {
              "foo": {
                "bar": "foo"
              }
            }
        """.trimIndent()
    }
}
