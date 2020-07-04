@file:Suppress("unused")

package com.jillesvangurp.kotlin4example

import mu.KLogger
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger { }

fun mdLink(title: String, target: String) = "[$title]($target)"
fun mdLink(page: Page) = mdLink(page.title, page.fileName)

fun md(repo: Repo, block: Kotlin4Example.() -> Unit) = lazyOf(Kotlin4Example.markdown(repo,block))

data class Repo(
    val repoUrl: String,
    val branch: String = "master",
    val sourcePaths: Set<String> = setOf("src/main/kotlin", "src/test/kotlin")) {

    fun md(block: Kotlin4Example.() -> Unit) = lazyOf(Kotlin4Example.markdown(this,block))
}

class Kotlin4Example(
    private val repo: Repo
    // val sourcePaths: MutableSet<String>,
    // private val repoUrl: String = "https://github.com/jillesvangurp/es-kotlin-wrapper-client"
) : AutoCloseable {
    private val buf = StringBuilder()

    private val patternForBlock = "block.*?\\{+".toRegex(RegexOption.MULTILINE)

    operator fun String.unaryPlus() {
        buf.appendln(this.trimIndent().trimMargin())
        buf.appendln()
    }

    fun mdCodeBlock(
        code: String,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80
    ) {
        var c = code.replace("    ", "  ")
        if (wrap) {
            c = c.lines().flatMap { line ->
                if (line.length <= lineLength) {
                    listOf<String>(line)
                } else {
                    line.chunked(lineLength)
                }
            }.joinToString("\n")
        }
        if (!allowLongLines && c.lines().firstOrNull { it.length > lineLength } != null) {
            logger.warn { "code block contains lines longer than 80 characters\n${(1 until lineLength).joinToString("") { "." } + "|"}\n$c" }
            throw IllegalArgumentException("code block exceeds line length of ")
        }

        buf.appendln("```$type\n$c\n```\n")
    }

    fun mdLink(clazz: KClass<*>): String {
        return mdLink(
            "`${clazz.simpleName!!}`",
            "${repo.repoUrl}/tree/${repo.branch}/${sourcePathForClass(clazz)}"
        )
    }

    fun mdLinkToRepoResource(title: String, relativeUrl: String) =
        mdLink(title, "${repo.repoUrl}/tree/${repo.branch}/$relativeUrl")

    fun snippetBlockFromClass(clazz: KClass<*>, snippetId: String) {
        val fileName = sourcePathForClass(clazz)
        snippetFromSourceFile(fileName, snippetId)
    }

    fun snippetFromSourceFile(
        fileName: String,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80
    ) {
        val snippetLines = mutableListOf<String>()
        val lines = File(fileName).readLines()
        var inSnippet = false
        for (line in lines) {
            if (inSnippet && line.contains(snippetId)) {
                break // break out of the loop
            }
            if (inSnippet) {
                snippetLines.add(line)
            }

            if (!inSnippet && line.contains(snippetId)) {
                inSnippet = true
            }
        }
        if (snippetLines.size == 0) {
            throw IllegalArgumentException("Snippet $snippetId not found in $fileName")
        }
        mdCodeBlock(
            snippetLines.joinToString("\n").trimIndent(),
            allowLongLines = allowLongLines,
            wrap = wrap,
            lineLength = lineLength
        )
    }

    /**
     * Figure out the source file name for the class so we can grab code from it. Looks in the source paths.
     */
    private fun sourcePathForClass(clazz: KClass<*>) =
        repo.sourcePaths.map { File(it, fileName(clazz)) }.firstOrNull() { it.exists() }?.path ?: throw IllegalArgumentException("source not found for ${clazz.qualifiedName}")

    /**
     * Figure out the (likely) file name for the class or inner class.
     */
    private fun fileName(clazz: KClass<*>) =
        clazz.qualifiedName!!.replace("\\$.*?$".toRegex(), "").replace('.', File.separatorChar) + ".kt"

    fun mdLinkToSelf(title: String = "Link to this source file"): String {
        val fn = this.sourceFileOfCaller() ?: throw IllegalStateException("source file not found")
        val path = repo.sourcePaths.map { File(it,fn) }.filter { it.exists() }.firstOrNull()?.path ?: throw IllegalStateException("file not found")
        return mdLink(title, "${repo.repoUrl}/tree/${repo.branch}/${path}")
    }

    fun <T> block(runBlock: Boolean = false, block: () -> T) {
        val callerSourceBlock = getCallerSourceBlock()
        if (callerSourceBlock == null) {
            // we are assuming a few things about the caller source:
            // - MUST be a class with its own source file
            // - The source file must be in the sourcePaths
            logger.warn { "Could not find code block from stack trace and sourcePath" }
        } else {
            mdCodeBlock(callerSourceBlock)
        }

        if (runBlock) {
            val response = block.invoke()

            val returnValue = response.toString()
            if (returnValue != "kotlin.Unit") {
                buf.appendln("Produces:\n")
                mdCodeBlock(returnValue, type = "")
            }
        }
    }

    fun blockWithOutput(
        allowLongLines: Boolean = false,
        allowLongLinesInOutput: Boolean = false,
        wrap: Boolean = false,
        wrapOutput: Boolean = false,
        lineLength: Int = 80,
        block: PrintWriter.() -> Unit
    ) {
        val callerSourceBlock = getCallerSourceBlock()

        val outputBuffer = ByteArrayOutputStream()
        val writer = PrintWriter(outputBuffer.writer())
        writer.use {
            block.invoke(writer)
            if (callerSourceBlock == null) {
                logger.warn { "Could not find code block from stack trace and sourcePath" }
            } else {
                mdCodeBlock(
                    code = callerSourceBlock,
                    allowLongLines = allowLongLines,
                    lineLength = lineLength,
                    wrap = wrap
                )
            }
            writer.flush()
        }
        val output = outputBuffer.toString()
        if (output.isNotEmpty()) {
            buf.appendln("Output:\n")
            mdCodeBlock(
                code = output,
                allowLongLines = allowLongLinesInOutput,
                wrap = wrapOutput,
                lineLength = lineLength,
                type = ""
            )
        }
    }

    private fun getCallerSourceBlock(): String? {
        val sourceFile = sourceFileOfCaller()

        val ste = getCallerStackTraceElement()
        val line = ste.lineNumber

        val lines = repo.sourcePaths.map {File(it, sourceFile).absolutePath}
            // the calculated fileName for the .class file does not match the source file for inner classes
            // so try to fix it by stripping the the Kt postfix
            .flatMap { listOf(it, it.replace("Kt.kt",".kt")) }
            .map { File(it) }
            .firstOrNull { it.exists() }?.readLines()
        return if (lines != null && line > 0) {
            // off by one error. Line numbers start at 1; list numbers start at 0
            val source = lines.subList(line - 1, lines.size).joinToString("\n")

            val allBlocks = patternForBlock.findAll(source)
            val match = allBlocks.first()
            val start = match.range.last
            var openCount = 1
            var index = start + 1
            while (openCount > 0 && index < source.length) {
                when (source[index++]) {
                    '{' -> openCount++
                    '}' -> openCount--
                }
            }
            if (index > start + 1 && index < source.length) {
                source.substring(start + 1, index - 1).trimIndent()
            } else {
                logger.warn { "no block found $start $index ${source.length} $openCount" }
                null
            }
        } else {
            logger.warn("no suitable file found for ${ste.fileName} ${ste.lineNumber}")
            null
        }
    }

    fun sourceFileOfCaller(): String? {
        val ste = getCallerStackTraceElement()
        val pathElements = ste.className.split('.')
        val relativeDir = pathElements.subList(0,pathElements.size-1).joinToString("${File.separatorChar}")
        return "$relativeDir${File.separatorChar}${ste.fileName}"

    }

    // internal fun sourceFileOfExampleCaller(): File? {
    //     val ste = getCallerStackTraceElement()
    //     println(ste.fileName)
    //     val pathElements = ste.className.split('.')
    //     val relativeDir = pathElements.subList(0,pathElements.size-1).joinToString("${File.separatorChar}")
    //     println(relativeDir)
    //     val fileName = (ste.className.replace("\\$.*?$".toRegex(), "").replace(
    //         '.',
    //         File.separatorChar
    //     ) + ".kt").replace("Kt.kt",".kt")
    //     return repo.sourcePaths.map { File(it, fileName) }.firstOrNull { it.exists() }
    // }

    internal fun getCallerStackTraceElement(): StackTraceElement {
        return Thread.currentThread()
            .stackTrace.first {
                !it.className.startsWith("java") &&
                    !it.className.startsWith("jdk.internal") &&
                    it.className != javaClass.name &&
                    it.className != "java.lang.Thread" &&
                    it.className != "io.inbot.eskotlinwrapper.manual.KotlinForExample" &&
                    it.className != "io.inbot.eskotlinwrapper.manual.KotlinForExample\$Companion" // edge case
            }
    }

    override fun close() {
    }

    companion object {
        fun markdown(
            repo: Repo,
            block: Kotlin4Example.() -> Unit
        ): String {
            val example = Kotlin4Example(repo)
            example.use(block)
            return example.buf.toString()
        }



        // fun markdownPageWithNavigation(page: Page, block: KotlinForExample.() -> Unit) {
        //     val index = pages.indexOf(page)
        //     val previous = if (index < 0) null else if (index == 0) null else pages[index - 1].fileName
        //     val next = if (index < 0) null else if (index == pages.size - 1) null else pages[index + 1].fileName
        //     val nav = listOfNotNull(
        //         if (!previous.isNullOrBlank()) mdLink("previous", previous) else null,
        //         if (!page.parent.isNullOrBlank()) mdLink("index", page.parent) else null,
        //         if (!next.isNullOrBlank()) mdLink("next", next) else null
        //     )
        //
        //     val example = KotlinForExample()
        //
        //     example.use(block)
        //     val md =
        //         """
        //         # ${page.title}
        //
        //         """.trimIndent().trimMargin() + "\n\n" + example.buf.toString()
        //
        //     val pageWithNavigationMd =
        //         (if (nav.isNotEmpty()) nav.joinToString(" | ") + "\n\n___\n\n" else "") +
        //             md + "\n" +
        //             (if (nav.isNotEmpty()) "___\n\n" + nav.joinToString(" | ") + "\n\n" else "") +
        //             """
        //                     This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the ${example.mdLinkToSelf(
        //                 "source file"
        //             )} from which this page is generated.
        //             """.trimIndent()
        //
        //     File(page.outputDir).mkdirs()
        //     File(page.outputDir, page.fileName).writeText(pageWithNavigationMd)
        //     if (page.emitBookPage) {
        //         File("epub").mkdirs()
        //         File("epub", page.fileName).writeText(md)
        //     }
        // }
    }
}