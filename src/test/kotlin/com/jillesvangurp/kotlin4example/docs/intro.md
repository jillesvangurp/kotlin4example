[![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
[![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)

This project implements [literate programming](https://en.wikipedia.org/wiki/Literate_programming) in Kotlin. Literate programming is useful
for documenting projects. Having working code in your documentation, ensures that the examples you include are correct 
and always up to date. And making it easy to include examples with your code lowers the barrier for writing good documentation.

This library is intended for anyone that publishes some kind of Kotlin library or code and wants to document their code using Markdown files that contain working examples.

## Get started

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

## Why another documentation tool?
    
When I started writing documentation for my [Kotlin Client for Elasticsearch](https://githubcom/jillesvangurp/es-kotlin-wrapper-client), I quickly discovered that keeping the 
examples in the documentation working was a challenge. I'd refactor or rename something which then would invalidate 
all my examples. Staying on top of that is a lot of work.

Instead of just using one of the many documentation tools out there that can grab chunks of source code based on 
some string marker, I instead came up with a **better solution**: Kotlin4example implements a **Markdown Kotlin DSL** that includes a few nifty features, including an `example` function that takes an arbitrary block of Kotlin code and turns it into a markdown code block.

So, to write documentation, you simply use the DSL to write your documentation in Kotlin. You don't have to write all of it in Kotlin of course; it can include regular markdown files as well. But when writing examples, you just write them in Kotlin and the library turns them into markdown code blocks.

There is of course more to this library. For more on that, check out the examples below. Which are of course generated with this library.
