# Kotlin4Example

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

## Usage

### Example blocks

With Kotlin4Example you can mix examples and markdown easily. 
An example is a code block
and it is executed by default. Because it is a code block,
 you are forced to ensure
it is syntactically correct and compiles. 

By executing it, you can further guarantee it does what it 
is supposed to and you can
intercept output and integrate that into your documentation.

For example:

```kotlin
print("Hello World")
```

This example prints **Hello World** when it executes. 

```kotlin
// out is an ExampleOutput instance
// with both stdout and the return
// value as a Result<T>. Any exceptions
// are captured as well.
val out = example {
  print("Hello World")
}
// this is how you can append arbitrary markdown
+"""
  This example prints **${out.stdOut}** when it executes. 
""".trimIndent()
```

### Suspending examples

If you use co-routines, you can use a suspendingExample

```kotlin
// runs the example in a runBlocking { .. }
suspendingExample {
  // call some suspending code
}
```

### Configuring blocks

```kotlin
// sometimes you just want to show but not run the code
example(
  runExample = false,
) {
  // your example goes here
}

// making sure the example fits in a web page
// long lines tend to look ugly in documentation
example(
  // default is 80
  lineLength = 120,
  // default is false
  wrap = true,
  // default is false
  allowLongLines = true,

) {
  // more code here
}
```

### Code snippets

While it is nice to have executable blocks, 
sometimes you just want to grab
code directly from a file. You can do that with snippets.

```kotlin
// BEGIN_MY_CODE_SNIPPET
println("Example code")
// END_MY_CODE_SNIPPET
exampleFromSnippet("readme.kt","MY_CODE_SNIPPET")
```

### Markdown

```kotlin
section("Section") {
  +"""
    You can use string literals, templates ${1+1}, 
    and [links](https://github.com/jillesvangurp/kotlin4example)
    or other markdown formatting.
  """.trimIndent()
}
// you can also just include markdown files
includeMdFile("intro.md")
// link to things in your git repository
mdLink(DocGenTest::class)
mdLinkToRepoResource("build file","build.gradle.kts")
```

## This README is generated

This README.md is of course created from kotlin code that 
runs as part of the test suite. You can look at the kotlin 
source code that generates this markdown [here](https://github.com/jillesvangurp/kotlin4example/tree/master/src/test/kotlin/com/jillesvangurp/kotlin4example/docs/readme.kt).

```kotlin
val readmeMarkdown by k4ERepo.md {
  // for larger bits of text, it's nice to load them from a markdown file
  includeMdFile("intro.md")

  section("Usage") {
    subSection("Example blocks") {
      +"""
        With Kotlin4Example you can mix examples and markdown easily. 
        An example is a code block
        and it is executed by default. Because it is a code block,
         you are forced to ensure
        it is syntactically correct and compiles. 
        
        By executing it, you can further guarantee it does what it 
        is supposed to and you can
        intercept output and integrate that into your documentation.
        
        For example:
      """.trimIndent()

      // a bit of kotlin4example inception here, but it works
example {
// out is an ExampleOutput instance
// with both stdout and the return
// value as a Result<T>. Any exceptions
// are captured as well.
val out = example {
  print("Hello World")
}
// this is how you can append arbitrary markdown
+"""
  This example prints **${out.stdOut}** when it executes. 
""".trimIndent()
      }
    }
    subSection("Suspending examples") {
      +"If you use co-routines, you can use a suspendingExample"

      example(runExample = false) {
        // runs the example in a runBlocking { .. }
        suspendingExample {
          // call some suspending code
        }
      }
    }
    subSection("Configuring blocks") {

      example(runExample = false) {
        // sometimes you just want to show but not run the code
        example(
          runExample = false,
        ) {
          // your example goes here
        }

        // making sure the example fits in a web page
        // long lines tend to look ugly in documentation
        example(
          // default is 80
          lineLength = 120,
          // default is false
          wrap = true,
          // default is false
          allowLongLines = true,

        ) {
          // more code here
        }
      }
    }
    subSection("Code snippets") {
      +"""
        While it is nice to have executable blocks, 
        sometimes you just want to grab
        code directly from a file. You can do that with snippets.
      """.trimIndent()

      example {
        // BEGIN_MY_CODE_SNIPPET
        println("Example code")
        // END_MY_CODE_SNIPPET
        exampleFromSnippet("readme.kt","MY_CODE_SNIPPET")
      }
    }
    subSection("Markdown") {
      // you can use our Kotlin DSL to structure your documentation.
      example(runExample = false) {
        section("Section") {
          +"""
            You can use string literals, templates ${1+1}, 
            and [links](https://github.com/jillesvangurp/kotlin4example)
            or other markdown formatting.
          """.trimIndent()
        }
        // you can also just include markdown files
        includeMdFile("intro.md")
        // link to things in your git repository
        mdLink(DocGenTest::class)
        mdLinkToRepoResource("build file","build.gradle.kts")
      }
    }
  }

  section("This README is generated") {
    +"""
      This README.md is of course created from kotlin code that 
      runs as part of the test suite. You can look at the kotlin 
      source code that generates this markdown ${mdLinkToSelf("here")}.
    """.trimIndent()

    // little string concatenation hack so it will read 
    // until the end marker instead of stopping here    
    exampleFromSnippet(
      "com/jillesvangurp/kotlin4example/docs/readme.kt",
      "README" + "CODE"
    )

    """
      And the code that actually writes the `README.md file` is a test:
    """.trimIndent()
    exampleFromSnippet(DocGenTest::class, "READMEWRITE")
  }

  includeMdFile("outro.md")
}
```

```kotlin
/**
 * The readme is generated when the tests run.
 */
class DocGenTest {
  @Test
  fun `generate readme for this project`() {
    val readmePage = Page("Kotlin4Example",fileName = "README.md")
    // readmeMarkdown is a lazy of the markdown content
    readmePage.write(markdown = readmeMarkdown)
  }
}
```

For more elaborate examples of using this library, checkout my 
[kt-search](https://github.com/jillesvangurp/kt-search) project. That 
project is where this project emerged from and all markdown in that project is generated by kotlin4example.


