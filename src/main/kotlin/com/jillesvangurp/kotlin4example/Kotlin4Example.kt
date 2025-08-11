@file:Suppress("unused")

package com.jillesvangurp.kotlin4example

import kotlinx.coroutines.runBlocking
import mu.KLogger
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger { }

fun mdLink(title: String, target: String) = "[$title]($target)"
@Deprecated("use mdPageLink", ReplaceWith("mdPageLink"))
fun mdLink(page: Page) = mdLink(page.title, page.fileName)

fun mdPageLink(page: Page) = mdLink(page.title, page.fileName)

fun md(sourceRepository: SourceRepository, block: Kotlin4Example.() -> Unit) =
    lazyOf(Kotlin4Example.markdown(sourceRepository, block))

@DslMarker
annotation class Kotlin4ExampleDSL

/**
 * A Kotlin DSL (Domain Specific Language) that you can use to generate markdown documentation
 * for your Kotlin code.
 *
 * The [sourceRepository] is used to create markdown links to files in your github repository.
 */
@Suppress("MemberVisibilityCanBePrivate")
@Kotlin4ExampleDSL
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

    /**
     * Create a section (## [title]) and use the [block] to specify what should be in the section.
     */
    fun section(title: String, block: (Kotlin4Example.() -> Unit)? = null) {
        buf.appendLine("## $title")
        buf.appendLine()
        block?.invoke(this)
    }

    /**
     * Create a sub section (### [title]) and use the [block] to specify what should be in the section.
     */
    fun subSection(title: String, block: (Kotlin4Example.() -> Unit)? = null) {
        buf.appendLine("### $title")
        buf.appendLine()
        block?.invoke(this)
    }

    /**
     * Create a sub section (### [title]) and use the [block] to specify what should be in the section.
     */
    fun subSubSection(title: String, block: (Kotlin4Example.() -> Unit)? = null) {
        buf.appendLine("#### $title")
        buf.appendLine()
        block?.invoke(this)
    }

    /**
     * Create a markdown code block for some [code] of a particular [type].
     *
     * Use [allowLongLines] turn off the check for lines longer than [lineLength]
     *
     * Use [wrap] to wrap your code. Note, all it does is add a new line at the 80th character.
     *
     * Use [reIndent] to change the indentation of the code to [reIndentSize]. Shorter indentation helps
     * keeping the lines short. Defaults to true
     */
    fun mdCodeBlock(
        code: String,
        type: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        reIndent: Boolean = true,
        reIndentSize: Int = 2
    ) {
        // reindenting is useful when including source snippets that are indented with 4 or 8 spaces
        var c = if(reIndent) code.reIndent(reIndentSize) else code
        if (wrap) {
            var l = 1
            c = c.lines().flatMap { line ->
                if (line.length <= lineLength) {
                    listOf(line)
                } else {
                    logger.warn { "wrapping line longer than 80 characters at line $l:\n$line" }
                    line.chunked(lineLength)
                }.also {
                    l++
                }
            }.joinToString("\n")
        }
        if (!allowLongLines) {
            var l = 1
            var error = 0
            c.lines().forEach {
                if (it.length > lineLength) {
                    logger.error { "code block contains lines longer than 80 characters at line $l:\n$it" }
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

    /**
     * Include the content of markdown file. The name the relative path to the directory your documentation is in.
     */
    fun includeMdFile(name: String) {
        val dir = sourceDirOfCaller()
        val file = "$dir${File.separatorChar}$name"
        val markDown = findContentInSourceFiles(file)?.joinToString("\n") ?: error("no such file $file")
        buf.append(markDown)
        buf.appendLine()
        buf.appendLine()
    }

    /**
     * Create a link to the source code of file that contains the [clazz] in your [SourceRepository].
     *
     * The [title] defaults to the class name.
     */
    fun mdLink(clazz: KClass<*>, title: String="`${clazz.simpleName!!}`"): String {
        return mdLink(
            title = title,
            target = sourceRepository.urlForFile(sourcePathForClass(clazz))
        )
    }

    /**
     * Create a link to a file in your [SourceRepository] with a [title].
     *
     * The [relativeUrl] should be relative to your source repository root.
     */
    fun mdLinkToRepoResource(title: String, relativeUrl: String) =
        mdLink(title, sourceRepository.repoUrl + relativeUrl)

    /**
     * Creates a link to the source file from which you are calling this.
     */
    fun mdLinkToSelf(title: String = "Link to this source file"): String {
        val fn = this.sourceFileOfCaller()
        val path = sourceRepository.sourcePaths.map { File(it, fn) }.firstOrNull { it.exists() }?.path
            ?: throw IllegalStateException("file not found")
        return mdLink(title, "${sourceRepository.repoUrl}/blob/${sourceRepository.branch}/${path}")
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

    /**
     * Creates a code block from code in the specified class.
     *
     * [snippetId] should be included in the source code in comments at the beginning and end of your example.
     *
     * Use [allowLongLines], [wrap], and [lineLength] to control the behavior for long lines (similar to [example].
     *
     * The [type] defaults to kotlin.
     */
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

    /**
     * Creates a code block for a [sourceFile] in your [SourceRepository]
     *
     * [snippetId] should be included in the source code in comments at the beginning and end of your example.
     *
     * Use [allowLongLines], [wrap], and [lineLength] to control the behavior for long lines (similar to [example].
     *
     * The [type] defaults to kotlin.
     */
    fun exampleFromSnippet(
        sourceFileName: String,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        type: String = "kotlin",
        reIndent: Boolean = true,
        reIndentSize: Int = 2
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
            lineLength = lineLength,
            reIndent=reIndent,
            reIndentSize=reIndentSize,
        )
    }

    @Deprecated("Use example, which now takes a suspending block by default", ReplaceWith("example(runExample, type, allowLongLines, wrap, lineLength, block)"))
    fun <T> suspendingExample(
        runExample: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        block: suspend BlockOutputCapture.() -> T
    ): ExampleOutput<T> {
       return example(
           runExample = runExample,
           type = type,
           allowLongLines = allowLongLines,
           wrap = wrap,
           lineLength = lineLength,
           block = block
       )
    }

    /**
     * Create a markdown code block for the code in the [block].
     *
     * The [block] takes a [BlockOutputCapture] as the parameter. You can use this to make
     * calls to print and println. The output is returned as part of [ExampleOutput] along
     * with the return value of your block.
     *
     * Use [runExample] to turn off execution of the block. This is useful if you want to show
     * code with undesirable side effects or that is slow to run. Defaults to true so it will
     * run your code unless change this.
     *
     * The [type] defaults to kotlin.
     *
     * Use [allowLongLines] turn off the check for lines longer than [lineLength]
     *
     * Use [wrap] to wrap your code. Note, all it does is add a new line at the 80th character
     */
    fun <T> example(
        runExample: Boolean = true,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        reIndent: Boolean = true,
        reIndentSize: Int = 2,
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
            type = type,
            reIndent = reIndent,
            reIndentSize = reIndentSize,
        )

        return ExampleOutput(returnVal, state.output().trimIndent())
    }

    /**
     * Add the output of your example to the markdown.
     *
     * ```
     */
    fun <T> renderExampleOutput(
        exampleOutput: ExampleOutput<T>,
        stdOutOnly: Boolean = true,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80,
        reIndent: Boolean = true,
        reIndentSize: Int = 2
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
                            type = "text",
                            reIndent = reIndent,
                            reIndentSize = reIndentSize,
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
                type = "text",
                reIndent = reIndent,
                reIndentSize = reIndentSize,
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

    fun table(headers: List<String>, rows: List<List<String>>) {
        require(rows.all { it.size == headers.size }) {
            "All rows must have the same number of columns as headers"
        }

        val allRows = listOf(headers) + rows
        val columnWidths = headers.indices.map { col ->
            allRows.maxOf { it[col].length }
        }

        fun formatRow(row: List<String>): String =
            row.mapIndexed { i, cell -> cell.padEnd(columnWidths[i]) }
                .joinToString(" | ", prefix = "| ", postfix = " |")

        val separator = columnWidths.joinToString(" | ", prefix = "| ", postfix = " |") {
            "-".repeat(it.coerceAtLeast(3)) // ensure minimum of 3 dashes for markdown
        }

        buf.appendLine(formatRow(headers))
        buf.appendLine(separator)
        rows.forEach { buf.appendLine(formatRow(it)) }
        buf.appendLine()
    }

    fun blockquote(text: String) {
        buf.appendLine("> ${text.trimIndent().replace("\n", "\n> ")}")
        buf.appendLine()
    }


    fun unorderedList(vararg items: String) {
        unorderedList(items.toList())
    }


    fun unorderedList(items: List<String>) {
        items.forEach { buf.appendLine("- ${it.trim()}") }
        buf.appendLine()
    }

    fun orderedList(vararg items: String) {
        orderedList(items.toList())
    }

    fun orderedList(items: List<String>) {
        items.forEachIndexed { i, item -> buf.appendLine("${i + 1}. ${item.trim()}") }
        buf.appendLine()
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

fun String.reIndent(indent: Int=2): String {
    val spaceFinder = "^(\\s+)".toRegex()
    return this.lines().firstOrNull {
        val whiteSpace = spaceFinder.find(it)?.value
        whiteSpace?.let {
            whiteSpace.length >= indent
        }  == true
    }?.let {
        val whiteSpace = spaceFinder.find(it)!!.groups[1]!!.value
        if(whiteSpace.length != indent ) {
            val originalIndent = " ".repeat(whiteSpace.length)
            val newIndent = " ".repeat(indent)
            this.replace(originalIndent, newIndent)
        } else {
            this
        }
    }?:this
}
