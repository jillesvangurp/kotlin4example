package com.jillesvangurp.kotlin4example

import java.io.ByteArrayOutputStream
import java.io.PrintWriter

/**
 * Simple facade that captures calls to print and println and collects
 * what would have been printed in a buffer.
 */
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