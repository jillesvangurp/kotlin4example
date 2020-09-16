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

fun md(sourceRepository: SourceRepository, block: Kotlin4Example.() -> Unit) = lazyOf(Kotlin4Example.markdown(sourceRepository,block))

class Kotlin4Example(
    private val sourceRepository: SourceRepository
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
        if(!allowLongLines) {
            var l=1
            var error=0
            c.lines().forEach {
                if(it.length > lineLength) {
                    logger.warn { "code block contains lines longer than 80 characters at line $l:\n$it" }
                    error++
                }
                l++
            }
            if(error>0) {
                throw IllegalArgumentException("code block exceeds line length of $lineLength")            }
        }

        buf.appendln("```$type\n$c\n```\n")
    }

    fun mdLink(clazz: KClass<*>): String {
        return mdLink(
            title = "`${clazz.simpleName!!}`",
            target = sourceRepository.urlForFile(sourcePathForClass(clazz))

        )
    }

    fun mdLinkToRepoResource(title: String, relativeUrl: String) =
        mdLink(title, sourceRepository.repoUrl + relativeUrl)

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

    fun mdLinkToSelf(title: String = "Link to this source file"): String {
        val fn = this.sourceFileOfCaller() ?: throw IllegalStateException("source file not found")
        val path = sourceRepository.sourcePaths.map { File(it, fn) }.firstOrNull { it.exists() }?.path ?: throw IllegalStateException("file not found")
        return mdLink(title, "${sourceRepository.repoUrl}/tree/${sourceRepository.branch}/${path}")
    }

    fun <T> block(runBlock: Boolean = false,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        block: () -> T) {
        val callerSourceBlock = getCallerSourceBlock()
        if (callerSourceBlock == null) {
            // we are assuming a few things about the caller source:
            // - MUST be a class with its own source file
            // - The source file must be in the sourcePaths
            logger.warn { "Could not find code block from stack trace and sourcePath" }
        } else {
            mdCodeBlock(
                code = callerSourceBlock,
                allowLongLines = allowLongLines,
                type = type,
                wrap = wrap,
                lineLength = lineLength
            )
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
        val sourceFile = sourceFileOfCaller() ?: throw IllegalStateException("cannot determine source file")

        val ste = getCallerStackTraceElement()
        val line = ste.lineNumber

        val lines = sourceRepository.sourcePaths.map {File(it, sourceFile).absolutePath}
            // the calculated fileName for the .class file does not match the source file for inner classes
            // so try to fix it by stripping the the Kt postfix
            .flatMap { listOf(it, it.replace("Kt.kt",".kt")) }
            .map { File(it) }
            .firstOrNull { it.exists() }?.readLines()
        return if (lines != null && line > 0) {
            // off by one error. Line numbers start at 1; list numbers start at 0
            val source = lines.subList(line - 1, lines.size).joinToString("\n")

            val allBlocks = patternForBlock.findAll(source)
            // FIXME this sometimes fails in a non reproducable way?
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

    private fun sourceFileOfCaller(): String? {
        val ste = getCallerStackTraceElement()
        val pathElements = ste.className.split('.')
        val relativeDir = pathElements.subList(0,pathElements.size-1).joinToString("${File.separatorChar}")
        return "$relativeDir${File.separatorChar}${ste.fileName}"

    }

    /**
     * Figure out the source file name for the class so we can grab code from it. Looks in the source paths.
     */
    private fun sourcePathForClass(clazz: KClass<*>) =
        sourceRepository.sourcePaths.map { File(it, fileName(clazz)) }.firstOrNull() { it.exists() }?.path ?: throw IllegalArgumentException("source not found for ${clazz.qualifiedName}")

    /**
     * Figure out the (likely) file name for the class or inner class.
     */
    private fun fileName(clazz: KClass<*>) =
        clazz.qualifiedName!!.replace("\\$.*?$".toRegex(), "").replace('.', File.separatorChar) + ".kt"


    private fun getCallerStackTraceElement(): StackTraceElement {
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
            sourceRepository: SourceRepository,
            block: Kotlin4Example.() -> Unit
        ): String {
            val example = Kotlin4Example(sourceRepository)
            example.use(block)
            return example.buf.toString()
        }
    }
}
