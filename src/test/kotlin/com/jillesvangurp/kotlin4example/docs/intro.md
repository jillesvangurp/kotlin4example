[![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
[![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)

This project implements [literate programming](https://en.wikipedia.org/wiki/Literate_programming) in Kotlin. Literate programming is useful
for documenting projects. Having working code in your documentation, ensures that the examples you include are correct 
and always up to date. And making it easy to include examples with your code lowers the barrier for writing good documentation.

This library is intended for anyone that publishes some kind of Kotlin library or code and wants to document their code using Markdown files that contain working examples.

Write your documentation using a kotlin markdown DSL. Use simple `example { // code goes here }` blocks to provide examples. Kotlin4example will generate nice markdown with the code inside that block added as code blocks. See below for a detailed introduction.

This README is of course generated with kotlin4example.

## Gradle

Add the dependency to your project and start writing some documentation. See below for some examples.
I tend to put my documentation code in my tests so running the tests produces the documentation as a side effect. 

```kotlin
implementation("com.github.jillesvangurp:kotlin4example:<version>")
```

You will also need to add the Jitpack repository:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```