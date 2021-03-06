[![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
[![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)

This project is an attempt at implementing [literate programming](https://en.wikipedia.org/wiki/Literate_programming) in Kotlin. 

## Get it

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

## Why another markdown snippet tool?
    
When I started writing documentation for my [Kotlin Client for Elasticsearch](https://githubcom/jillesvangurp/es-kotlin-wrapper-client), I quickly discovered that copying bits of source code quickly leads to broken or inaccurate documentation samples. Having to constantly chase bugs and outdated code samples is an obstacle to writing documentation.

I fixed it by hacking together a solution to grab code samples from Kotlin through reflection and by making some assumptions about where source files are in a typical gradle project.
    
There are other tools that solve this problem. Usually this works by putting some strings in comments in your code and using some tool to dig out code snippets from the source code.
     
And there's of course nothing wrong with that approach and Kotlin4example actually also supports this. However, I wanted more. I wanted to actually run the snippets, be able to grab the output, and generate documentation using the Github flavor of markdown. Also, I did not want to deal with keeping track of snippet ids, their code comments, etc. Instead, I wanted to mix code and documentation and be able to refactor both code and documentation easily.

## How Does it work?

Kotlin has multi line strings, templating, and some built in support for creating your own DSLs. So, I created a simple Kotlin DSL that generates markdown by concatenating strings (with Markdown) and executable kotlin blocks. The executable blocks basically contain the source code I want to show in a Markdown code block. So, the block figures out the source file it is in and the exact line it starts at and we grab exactly those lines and turn them into a markdown code block. We can also grab the output (optional) when it runs and can grab that.
