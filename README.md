# Kotlin4Example

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
    
When I started writing documentation for my [Kotlin Client for Elasticsearch](https://githubcom/jillesvangurp/es-kotlin-wrapper-client), I quickly discovered that copying bits of source code to quickly leads to broken or inaccurate documentation samples. Having to constantly chase bugs and outdated code samples is a huge obstacle to writing documentation.

I fixed it by hacking together a solution to grab code samples from Kotlin through reflection and by making some assumptions about where source files are in a typical gradle project.
    
There are other tools that solve this problem. Usually this works by putting some strings in comments in your code and using some tool to dig out code snippets from the source code. Kotlin4example actually also supports this.
    
And there's of course nothing wrong with that approach. However, I wanted more. I wanted to actually run the snippets, be able to grab the output, and generate documentation using the Github flavor of markdown. Also, I did not want to deal with keeping track of snippet ids, their code comments, etc. Instead, I wanted to mix code and documentation and be able to refactor both code and documentation easily.

## How Does it work?

Kotlin has multi line strings, templating, and some built in constructions for creating your own DSLs. So, I created a simple Kotlin DSL that generates markdown by concatenating strings (with Markdown) and executable kotlin blocks. The executable blocks basically contain the source code I want to show in a Markdown code block. So, the block figures out the source file it is in and the exact line it starts at and we grab exactly those lines and turn them into a markdown code block. We can also grab the output (optional) when it runs and can grab that.

## Example

```kotlin
// documentation inception
// this is technically a block within a block, just so I can show you
// how you would use it.
block {
  println("Hello World")
}
```

Here's the same block as above running as part of this 
[readme.kt](https://github.com/jillesvangurp/kotlin4example/tree/master/src/test/kotlin/com/jillesvangurp/kotlin4example/docs/readme.kt) file.

```kotlin
println("Hello World")
```

Captured Output:

```
Hello World

```

As you can see, we indeed show a pretty printed block, ran it, and
grabbed the output. Observant readers will also note that the nested 
block above did not run. The reason for this is that the outer `block` 
call for that has a parameter that you can use to prevent this. 
If you look at the source code for the readme, you will see we used 
`block(runBlock = false)`

We can also return a value from the block and capture that:

```kotlin
fun aFunctionThatReturnsAnInt() = 1 + 1

// call the function to make the block return something
aFunctionThatReturnsAnInt()
```

->

```
2
```

Note how that captured the return value and printed that 
without us using `print` or `println`.

## This README is generated

This README.md is actually created from kotlin code that 
runs as part of the test suite. You can look at the kotlin 
source code that generates this markdown [here](https://github.com/jillesvangurp/kotlin4example/tree/master/src/test/kotlin/com/jillesvangurp/kotlin4example/docs/readme.kt).

```kotlin
val readme by k4ERepo.md {
  // for larger bits of text, it's nice to load them from a markdown file
  includeMdFile("intro.md")

  section("Example") {
    block(runBlock = false) {
      // documentation inception
      // this is technically a block within a block, just so I can show you
      // how you would use it.
      block {
        println("Hello World")
      }
    }
    // but of course you can inline a Kotlin multiline string with some markdown
    +"""
      Here's the same block as above running as part of this 
      ${mdLinkToSelf("readme.kt")} file.
    """

    block {
      println("Hello World")
    }

    +"""
      As you can see, we indeed show a pretty printed block, ran it, and
      grabbed the output. Observant readers will also note that the nested 
      block above did not run. The reason for this is that the outer `block` 
      call for that has a parameter that you can use to prevent this. 
      If you look at the source code for the readme, you will see we used 
      `block(runBlock = false)`
      
      We can also return a value from the block and capture that:
    """

    block {
      fun aFunctionThatReturnsAnInt() = 1 + 1

      // call the function to make the block return something
      aFunctionThatReturnsAnInt()
    }

    +"""
      Note how that captured the return value and printed that 
      without us using `print` or `println`.
    """

  }

  section("This README is generated") {
    +"""
      This README.md is actually created from kotlin code that 
      runs as part of the test suite. You can look at the kotlin 
      source code that generates this markdown ${mdLinkToSelf("here")}.
    """.trimIndent()

    // little hack so it will read until the end marker
    snippetFromSourceFile(
      "com/jillesvangurp/kotlin4example/docs/readme.kt",
      "README" + "CODE"
    )

    """
      And the code that actually writes the file is a test:
    """.trimIndent()
    snippetBlockFromClass(DocGenTest::class, "READMEWRITE")
  }

  includeMdFile("outro.md")
}
```

```kotlin
@Test
fun `generate readme for this project`() {
  val readmeMd = Page("Kotlin4Example",fileName = "README.md")
  readmeMd.write(markdown = readme)
}
```

For more elaborate examples of using this library, checkout my 
[Kotlin Client for Elasticsearch](https://github.com/jillesvangurp/es-kotlin-wrapper-client) project. That 
project is where this project emerged from and all markdown in that project is generated by kotlin4example.

## Development status & roadmap

This is still a work in progress but it's also the basis for documentation for a few projects I maintain.       
So, API stability is at this point getting more important to me. Which means it should be fine for you as well. 

I'm planning to build this out over time with more useful features. My intention is not to replace markdown
with a Kotlin DSL. But instead to generate e.g. markdown links with kotlin and have a
few other conveniences. Also, I'm thinking of eventually self publishing some of the documentation for my 
projects in epub form and have started experimenting with generating scripts to unleash pandoc on my 
generated markdown.

Finally, most of the things you document are also the things you should be testing and there is an argument
to be made for turning this into a proper test framework. Projects like [kotest](https://github.com/kotest/kotest)
could be combined with this to accomplish that I guess.

