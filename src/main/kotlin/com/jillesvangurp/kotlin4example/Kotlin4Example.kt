@file:Suppress("unused")

package com.jillesvangurp.kotlin4example

import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger { }

fun mdLink(title: String, target: String) = "[$title]($target)"
fun mdLink(page: Page) = mdLink(page.title, page.fileName)

fun md(sourceRepository: SourceRepository, block: Kotlin4Example.() -> Unit) =
    lazyOf(Kotlin4Example.markdown(sourceRepository, block))

class BlockOutputCapture {
    private val byteArrayOutputStream = ByteArrayOutputStream()
    private val printWriter = PrintWriter(byteArrayOutputStream)

    fun print(message: Any?) {
        printWriter.print(message)
    }

    fun println(message: Any?) {
        printWriter.println(message)
    }

    fun output(): String {
        printWriter.flush()
        return byteArrayOutputStream.toString()
    }

    fun reset() {
        printWriter.flush()
        byteArrayOutputStream.reset()
    }
}

data class ExampleOutput<T>(
    val result: Result<T?>,
    val stdOut: String,
)

@Suppress("MemberVisibilityCanBePrivate")
class Kotlin4Example(
    private val sourceRepository: SourceRepository
) {
    private val buf = StringBuilder()

    private val patternForBlock = "(suspendingBlock|block|example|suspendingExample).*?\\{+".toRegex(RegexOption.MULTILINE)

    /**
     * Append some arbitrary markdown. Tip, you can use raw strings and string templates """${1+1}"""
     */
    operator fun String.unaryPlus() {
        buf.appendLine(this.trimIndent().trimMargin())
        buf.appendLine()
    }

    fun section(title: String, block: (Kotlin4Example.() -> Unit)? = null) {
        buf.appendLine("## $title")
        buf.appendLine()
        block?.invoke(this)
    }

    fun subSection(title: String, block: (Kotlin4Example.() -> Unit)? = null) {
        buf.appendLine("### $title")
        buf.appendLine()
        block?.invoke(this)
    }

    private fun mdCodeBlock(
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
                    listOf(line)
                } else {
                    line.chunked(lineLength)
                }
            }.joinToString("\n")
        }
        if (!allowLongLines) {
            var l = 1
            var error = 0
            c.lines().forEach {
                if (it.length > lineLength) {
                    logger.warn { "code block contains lines longer than 80 characters at line $l:\n$it" }
                    error++
                }
                l++
            }
            if (error > 0) {
                throw IllegalArgumentException("code block exceeds line length of $lineLength")
            }
        }

        buf.appendLine("```$type\n$c\n```\n")
    }

    fun includeMdFile(name: String) {
        val dir = sourceDirOfCaller()
        val file = "$dir${File.separatorChar}$name"
        val markDown = findContentInSourceFiles(file)?.joinToString("\n") ?: error("no such file $file")
        buf.append(markDown)
        buf.appendLine()
        buf.appendLine()
    }

    fun mdLink(clazz: KClass<*>): String {
        return mdLink(
            title = "`${clazz.simpleName!!}`",
            target = sourceRepository.urlForFile(sourcePathForClass(clazz))

        )
    }

    fun mdLinkToRepoResource(title: String, relativeUrl: String) =
        mdLink(title, sourceRepository.repoUrl + relativeUrl)

    fun mdLinkToSelf(title: String = "Link to this source file"): String {
        val fn = this.sourceFileOfCaller()
        val path = sourceRepository.sourcePaths.map { File(it, fn) }.firstOrNull { it.exists() }?.path
            ?: throw IllegalStateException("file not found")
        return mdLink(title, "${sourceRepository.repoUrl}/tree/${sourceRepository.branch}/${path}")
    }

    @Deprecated("Use exampleFromSnippet", ReplaceWith("exampleFromSnippet(clazz, snippetId, allowLongLines, wrap, lineLength, type)"))
    fun snippetBlockFromClass(
        clazz: KClass<*>,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        type: String = "kotlin"
    ) {
        exampleFromSnippet(clazz, snippetId, allowLongLines, wrap, lineLength, type)
    }
    fun exampleFromSnippet(
        clazz: KClass<*>,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        type: String = "kotlin"
    ) {
        val fileName = sourcePathForClass(clazz)
        exampleFromSnippet(
            sourceFileName = fileName,
            snippetId = snippetId,
            allowLongLines = allowLongLines,
            wrap = wrap,
            lineLength = lineLength,
            type = type
        )
    }

    @Deprecated("Use exampleFromSnippet", ReplaceWith("exampleFromSnippet(fileName,snippetId, allowLongLines, wrap, lineLength, type)"))
    fun snippetFromSourceFile(
        fileName: String,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        type: String = "kotlin"
    ) {
        exampleFromSnippet(fileName,snippetId, allowLongLines, wrap, lineLength, type)
    }

    fun exampleFromSnippet(
        sourceFileName: String,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        type: String = "kotlin"
    ) {
        val snippetLines = mutableListOf<String>()

        val lines = File(sourceRepository.sourcePaths.map { File(it, sourceFileName) }.firstOrNull { it.exists() }?.path
            ?: sourceFileName
        ).readLines()
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
            throw IllegalArgumentException("Snippet $snippetId not found in $sourceFileName")
        }
        mdCodeBlock(
            snippetLines.joinToString("\n").trimIndent(),
            type = type,
            allowLongLines = allowLongLines,
            wrap = wrap,
            lineLength = lineLength
        )
    }

    fun <T> suspendingExample(
        runExample: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        block: suspend BlockOutputCapture.() -> T
    ): ExampleOutput<T> {
        val state = BlockOutputCapture()
        val returnVal = try {
            if (runExample) {
                runBlocking {
                    Result.success(block.invoke(state))
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        val callerSourceBlock =
            getCallerSourceBlock() ?: throw IllegalStateException("source block could not be extracted")
        mdCodeBlock(
            code = callerSourceBlock,
            allowLongLines = allowLongLines,
            wrap = wrap,
            lineLength = lineLength,
            type = type
        )

        return ExampleOutput(returnVal, state.output())
    }
    fun <T> example(
        runExample: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        block: BlockOutputCapture.() -> T
    ): ExampleOutput<T> {
        val state = BlockOutputCapture()
        val returnVal = try {
            if (runExample) {
                Result.success(block.invoke(state))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        val callerSourceBlock =
            getCallerSourceBlock() ?: throw IllegalStateException("source block could not be extracted")
        mdCodeBlock(
            code = callerSourceBlock,
            allowLongLines = allowLongLines,
            wrap = wrap,
            lineLength = lineLength,
            type = type
        )

        return ExampleOutput(returnVal, state.output().trimIndent())
    }

    fun <T> renderExampleOutput(
        exampleOutput: ExampleOutput<T>,
        stdOutOnly: Boolean = true,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
    ) {
        if(!stdOutOnly) {
            exampleOutput.result.let { r ->
                r.getOrNull()?.let { returnValue ->
                    if(returnValue !is Unit) {
                        mdCodeBlock(
                            returnValue.toString(),
                            allowLongLines = allowLongLines,
                            wrap = wrap,
                            lineLength = lineLength,
                            type = "text"
                        )
                    }
                }
            }
        }
        exampleOutput.stdOut.takeIf { it.isNotBlank() }?.let {
            mdCodeBlock(
                it,
                allowLongLines = allowLongLines,
                wrap = wrap,
                lineLength = lineLength,
                type = "text"
            )
        }
    }

    @Deprecated("Use the new example function", ReplaceWith("""renderExampleOutput(example(runBlock,type,allowLongLines,wrap,lineLength,block),!captureBlockReturnValue,allowLongLines,wrap,lineLength)"""))
    fun <T> block(
        runBlock: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        printStdOut: Boolean = true,
        captureBlockReturnValue: Boolean = true,
        stdOutPrefix: String = "Captured Output:",
        returnValuePrefix: String = "->",
        lineLength: Int = 80,
        block: BlockOutputCapture.() -> T
    ) {
        val state = BlockOutputCapture()
        @Suppress("DEPRECATION")
        block(
            allowLongLines = allowLongLines,
            type = type,
            wrap = wrap,
            lineLength = lineLength,
            runBlock = runBlock,
            block = block,
            blockCapture = state,
            returnValuePrefix = returnValuePrefix,
            printStdOut = printStdOut,
            captureBlockReturnValue = captureBlockReturnValue,
            stdOutPrefix = stdOutPrefix
        )
    }

    @Deprecated("Use the new example function", ReplaceWith("""renderExampleOutput(example(runBlock,type,allowLongLines,wrap,lineLength,block),!captureBlockReturnValue,allowLongLines,wrap,lineLength)"""))
    fun <T> block(
        runBlock: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        printStdOut: Boolean = true,
        stdOutPrefix: String = "Captured Output:",
        returnValuePrefix: String = "->",
        lineLength: Int = 80,
        captureBlockReturnValue: Boolean = true,
        blockCapture: BlockOutputCapture,
        block: BlockOutputCapture.() -> T
    ) {
        val callerSourceBlock =
            getCallerSourceBlock() ?: throw IllegalStateException("source block could not be extracted")
        mdCodeBlock(
            code = callerSourceBlock,
            allowLongLines = allowLongLines,
            type = type,
            wrap = wrap,
            lineLength = lineLength
        )

        if (runBlock) {
            val response = block.invoke(blockCapture)
            if (response !is Unit) {
                if (captureBlockReturnValue) {
                    buf.appendLine("$returnValuePrefix\n")
                    mdCodeBlock(response.toString(), type = "")
                }
            }
        }

        // if you have runBlock == false, no output can be produced
        if (printStdOut && runBlock) {
            val output = blockCapture.output()
            blockCapture.reset()
            if (output.isNotBlank()) {
                buf.appendLine("$stdOutPrefix\n")
                mdCodeBlock(
                    code = output,
                    allowLongLines = allowLongLines,
                    wrap = wrap,
                    lineLength = lineLength,
                    type = ""
                )
            }
        }
    }

    @Deprecated("Use the new suspendingExample function", ReplaceWith("""renderExampleOutput(suspendingExample(runBlock,type,allowLongLines,wrap,lineLength,block),!captureBlockReturnValue,allowLongLines,wrap,lineLength)"""))
    fun <T> suspendingBlock(
        runBlock: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        printStdOut: Boolean = true,
        stdOutPrefix: String = "Captured Output:",
        returnValuePrefix: String = "->",
        lineLength: Int = 80,
        captureBlockReturnValue: Boolean = true,
        block: suspend BlockOutputCapture.() -> T
    ) {
        val state = BlockOutputCapture()
        @Suppress("DEPRECATION")
        suspendingBlock(
            allowLongLines = allowLongLines,
            type = type,
            wrap = wrap,
            lineLength = lineLength,
            runBlock = runBlock,
            block = block,
            blockCapture = state,
            returnValuePrefix = returnValuePrefix,
            printStdOut = printStdOut,
            captureBlockReturnValue = captureBlockReturnValue,
            stdOutPrefix = stdOutPrefix
        )
    }

    @Deprecated("Use the new suspendingExample function", ReplaceWith("""renderExampleOutput(suspendingExample(runBlock,type,allowLongLines,wrap,lineLength,block),!captureBlockReturnValue,allowLongLines,wrap,lineLength)"""))
    fun <T> suspendingBlock(
        runBlock: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        printStdOut: Boolean = true,
        stdOutPrefix: String = "Captured Output:",
        returnValuePrefix: String = "->",
        lineLength: Int = 80,
        blockCapture: BlockOutputCapture,
        captureBlockReturnValue: Boolean = true,
        block: suspend BlockOutputCapture.() -> T
    ) {
        val callerSourceBlock =
            getCallerSourceBlock() ?: throw IllegalStateException("source block could not be extracted")
        mdCodeBlock(
            code = callerSourceBlock,
            allowLongLines = allowLongLines,
            type = type,
            wrap = wrap,
            lineLength = lineLength
        )

        if (runBlock) {
            val response = runBlocking {
                block.invoke(blockCapture)
            }

            if (response !is Unit && captureBlockReturnValue) {
                buf.appendLine("$returnValuePrefix\n")
                mdCodeBlock(response.toString(), type = "")
            }
        }

        // if you have runBlock == false, no output can be produced
        if (printStdOut && runBlock) {
            val output = blockCapture.output()
            blockCapture.reset()
            if (output.isNotBlank()) {
                buf.appendLine("$stdOutPrefix\n")
                mdCodeBlock(
                    code = output,
                    allowLongLines = allowLongLines,
                    wrap = wrap,
                    lineLength = lineLength,
                    type = ""
                )
            }
        }
    }

    private fun findContentInSourceFiles(sourceFile: String) =
        sourceRepository.sourcePaths.map { File(it, sourceFile).absolutePath }
            // the calculated fileName for the .class file does not match the source file for inner classes
            // so try to fix it by stripping the Kt postfix
            .flatMap { listOf(it, it.replace("Kt.kt", ".kt")) }
            .map { File(it) }
            .firstOrNull { it.exists() }?.readLines()


    private fun getCallerSourceBlock(): String? {
        val sourceFile = sourceFileOfCaller()

        val ste = getCallerStackTraceElement()
        val line = ste.lineNumber

        val lines = findContentInSourceFiles(sourceFile)

        return if (lines != null && line > 0) {
            // Off by one error. Line numbers start at 1; list numbers start at 0
            val source = lines.subList(line - 1, lines.size).joinToString("\n")

            val allBlocks = patternForBlock.findAll(source)
            // FIXME this sometimes fails in a non reproducible way?
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

    private fun sourceFileOfCaller(): String {
        val ste = getCallerStackTraceElement()
        val pathElements = ste.className.split('.')
        val relativeDir = pathElements.subList(0, pathElements.size - 1).joinToString("${File.separatorChar}")
        return "$relativeDir${File.separatorChar}${ste.fileName}"
    }

    private fun sourceDirOfCaller(): String {
        val ste = getCallerStackTraceElement()
        val pathElements = ste.className.split('.')
        return pathElements.subList(0, pathElements.size - 1).joinToString("${File.separatorChar}")
    }

    /**
     * Figure out the source file name for the class, so we can grab code from it. Looks in the source paths.
     */
    private fun sourcePathForClass(clazz: KClass<*>) =
        sourceRepository.sourcePaths.map { File(it, fileName(clazz)) }.firstOrNull { it.exists() }?.path
            ?: throw IllegalArgumentException("source not found for ${clazz.qualifiedName}")

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

    companion object {
        fun markdown(
            sourceRepository: SourceRepository,
            block: Kotlin4Example.() -> Unit
        ): String {
            val example = Kotlin4Example(sourceRepository)
            example.apply(block)
            return example.buf.toString()
        }
    }
}
