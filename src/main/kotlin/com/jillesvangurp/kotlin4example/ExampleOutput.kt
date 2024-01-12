package com.jillesvangurp.kotlin4example

/**
 * When you use [Kotlin4Example.example] it uses this as the return value.
 */
data class ExampleOutput<T>(
    val result: Result<T?>,
    val stdOut: String,
)