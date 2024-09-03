# Kotlin4Example

[![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
[![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)

This project implements [literate programming](https://en.wikipedia.org/wiki/Literate_programming) in Kotlin. Literate programming is useful
for documenting projects. Having working code in your documentation, ensures that the examples you include are correct 
and always up to date. And making it easy to include examples with your code lowers the barrier for writing good documentation.

This library is intended for anyone that publishes some kind of Kotlin library or code and wants to document their code using Markdown files that contain working examples.

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

## Why another documentation tool?
    
When I started writing documentation for my [Kotlin Client for Elasticsearch](https://githubcom/jillesvangurp/es-kotlin-wrapper-client), I quickly discovered that keeping the 
examples in the documentation working was a challenge. I'd refactor or rename something which then would invalidate 
all my examples. Staying on top of that is a lot of work.

Instead of just using one of the many documentation tools out there that can grab chunks of source code based on 
some string marker, I instead came up with a **better solution**: Kotlin4example implements a **Markdown Kotlin DSL** that includes a few nifty features, including an `example` function that takes an arbitrary block of Kotlin code and turns it into a markdown code block.

So, to write documentation, you simply use the DSL to write your documentation in Kotlin. You don't have to write all of it in Kotlin of course; it can include regular markdown files as well. But when writing examples, you just write them in Kotlin and the library turns them into markdown code blocks.

There is of course more to this library. For more on that, check out the examples below. Which are of course generated with this library.

## Getting Started

After adding this library to your (test) dependencies, you can start adding code 
to generate markdown. 

### Creating a SourceRepository

The first thing you need is a `SourceRepository` definition. This is needed to tell
kotlin4example about your repository.

Some of the functions in kotlin4example construct links to files in your github repository,
or lookup code from files in your source code. 

```kotlin
val k4ERepo = SourceRepository(
  // used to construct markdown links to files in your repository
  repoUrl = "https://github.com/jillesvangurp/kotlin4example",
  // default is main
  branch = "master",
  // this is the default
  sourcePaths = setOf(
    "src/main/kotlin",
    "src/test/kotlin"
  )
)
```

### Creating markdown

```kotlin
val myMarkdown = k4ERepo.md {
  section("Introduction")
  +"""
    Hello world!
  """.trimIndent()
}
println(myMarkdown)
```

This will generate some markdown that looks as follows.

```markdown
## Introduction

Hello world!


```

### Using your Markdown to create a page

Kotlin4example has a simple page abstraction that you
can use to organize your markdown content into pages and files

```kotlin
val page = Page(title = "Hello!", fileName = "hello.md")
// creates hello.md
page.write(myMarkdown)
```

### This README is generated

This README.md is of course created from kotlin code that 
runs as part of the test suite. You can look at the kotlin 
source code that generates this markdown [here](https://github.com/jillesvangurp/kotlin4example/blob/master/src/test/kotlin/com/jillesvangurp/kotlin4example/docs/readme.kt).

The code that writes the `README.md file` is as follows:

```kotlin
/**
 * The readme is generated when the tests run.
 */
class DocGenTest {
  @Test
  fun `generate readme for this project`() {
    val readmePage = Page(
      title = "Kotlin4Example",
      fileName = "README.md"
    )
    // readmeMarkdown is a lazy of the markdown content
    readmePage.write(markdown = readmeMarkdown)
  }
}
```

Here's a link to the source code on Github: [`DocGenTest`](https://github.com/jillesvangurp/kotlin4example/blob/master/src/test/kotlin/com/jillesvangurp/kotlin4example/DocGenTest.kt)

## Usage

### Example blocks

With Kotlin4Example you can mix examples and markdown easily. 
An example is a Kotlin code block. Because it is a code block,
 you are forced to ensure it is syntactically correct and that it compiles. 

By executing the block (you can disable this), you can further guarantee it does what it 
is supposed to and you can intercept output and integrate that into your 
documentation as well

For example:

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

The block you pass to example can be a suspending block; so you can create examples for 
your co-routine libraries too. Kotlin4example uses `runBlocking` to run your examples.

When you include the above in your Markdown it will render as follows:

```kotlin
print("Hello World")
```

This example prints **Hello World** when it executes. 

### Configuring examples

Sometimes you just want to show but not run the code. You can control this with the 
`runExample` parameter.

```kotlin
//
example(
  runExample = false,
) {
  // your code goes here
  // but it won't run
}
```

The library imposes a default line length of 80 characters on your examples. The 
reason is that code blocks with long lines look ugly on web pages. E.g. Github will give 
you a horizontal scrollbar.

You can of course turn this off or turn on the built in wrapping (wraps at the 80th character) 

```kotlin

// making sure the example fits in a web page
// long lines tend to look ugly in documentation
example(
  // use longer line length
  // default is 80
  lineLength = 120,
  // wrap lines that are too long
  // default is false
  wrap = true,
  // don't fail on lines that are too long
  // default is false
  allowLongLines = true,

  ) {
  // your code goes here
}
```

### Code snippets

While it is nice to have executable blocks as examples, 
sometimes you just want to grab
code directly from some Kotlin file. You can do that with snippets.

```kotlin
// BEGIN_MY_CODE_SNIPPET
println("Example code that shows in a snippet")
// END_MY_CODE_SNIPPET
```

```kotlin
println("Example code that shows in a snippet")
```

The `BEGIN_` and `END_` prefix are optional but I find it helps readability.

You include the code in your markdown as follows:

```kotlin
exampleFromSnippet(
  sourceFileName = "com/jillesvangurp/kotlin4example/docs/readme.kt",
  snippetId = "MY_CODE_SNIPPET"
)
```

### Misc Markdown

```kotlin
section("Section") {
  subSection("Sub Section") {
    +"""
      You can use string literals, templates ${1 + 1}, 
      and [links](https://github.com/jillesvangurp/kotlin4example)
      or other markdown formatting.
    """.trimIndent()
  }
}
section("Links") {

  // you can also just include markdown files
  // useful if you have a lot of markdown
  // content without code examples
  includeMdFile("intro.md")

  // link to things in your git repository
  mdLink(DocGenTest::class)

  // link to things in one of your source directories
  // you can customize where it looks in SourceRepository
  mdLinkToRepoResource(
    title = "A file",
    relativeUrl = "com/jillesvangurp/kotlin4example/Kotlin4Example.kt"
  )

  val anotherPage = Page("Page 2", "page2.md")
  // link to another page in your manual
  mdPageLink(anotherPage)

  // and of course you can link to your self
  mdLinkToSelf("This class")
}
```

### Source code blocks

You can add your own source code blocks as well.

```kotlin
mdCodeBlock(
  code = """
    Useful if you have some non kotlin code that you want to show
  """.trimIndent(),
  type = "text"
)
```

## Advanced topics

### Context receivers

A new feature in Kotlin that you currently have to opt into is context receivers.

Context receivers are useful for processing the output of your examples since you typically
need Kotlin4Example when you use the ExampleOutput.

I don't want
to force people to opt into context receivers yet but it's easy to add this yourself.

Simply add a simple extension function like this:.

```kotlin
context(Kotlin4Example)!
fun ExampleOutput<*>.printStdOut() {
  +"""
    This prints:
  """.trimIndent()
 
  mdCodeBlock(stdOut, type = "text", wrap = true)
}
```

And then you can use it `example { 1+1}.printStdOut()`.

To opt into context receivers, add this to your build file

```kotlin
kotlin {
  compilerOptions {
    freeCompilerArgs= listOf("-Xcontext-receivers")
  }
}        
```

For more elaborate examples of using this library, checkout my 
[kt-search](https://github.com/jillesvangurp/kt-search) project. That 
project is where this project emerged from and all markdown in that project is generated by kotlin4example. Give it a try on one of your own projects and let me know what you think.

## Projects that use kotlin4example

- [kt-search](https://github.com/jillesvangurp/kt-search)
- [kotlin-opencage-client](https://github.com/jillesvangurp/kotlin-opencage-client)
- [json-dsl](https://github.com/jillesvangurp/json-dsl)

Create a pull request against [outro.md](https://github.com/jillesvangurp/kotlin4example/blob/master/src/test/kotlin/com/jillesvangurp/kotlin4example/docs/outro.md) if you want to add your project here.

