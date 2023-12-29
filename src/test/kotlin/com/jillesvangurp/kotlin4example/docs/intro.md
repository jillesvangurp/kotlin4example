[![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
[![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)

This project is an attempt at implementing [literate programming](https://en.wikipedia.org/wiki/Literate_programming) in Kotlin. Literate programming is useful
for documenting projects. You mix working code and documentation. 

The practice of copying code snippets to code blocks
inside markdown files is very brittle and leads to code that easily breaks. This project
solves this by generating the markdown from Kotlin with a simple DSL and provides
you with the tools to construct examples from working code.


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
    
When I started writing documentation for my [Kotlin Client for Elasticsearch](https://githubcom/jillesvangurp/es-kotlin-wrapper-client), I quickly discovered that keeping the 
examples working was a big challenge. 

I fixed it by hacking together a solution to grab code samples from Kotlin through reflection and by making some assumptions about where source files are in a typical gradle project on github.
    
There are other tools that solve this problem. Usually this works by putting some strings in comments in your code and using some tool to dig out code snippets from the source code.
     
And there's of course nothing wrong with that approach and Kotlin4example actually also supports this. However, I wanted more. I wanted to actually run the snippets, be able to grab the output, and generate documentation using the Github flavor of markdown. Also, I did not want to deal with keeping track of snippet ids, their code comments, etc. Instead, I wanted to mix code and documentation and be able to refactor both code and documentation easily.
