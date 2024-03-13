# Kotlin4Example

[![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
[![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)

This project implements [literate programming](https://en.wikipedia.org/wiki/Literate_programming) in Kotlin. Literate programming is useful
for documenting projects. Having working code in your documentation, ensures that the examples you include are correct 
and always up to date. And making it easy to include examples with your code lowers the barrier for writing good documentation.

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
some string marker, I instead came up with a better solution.

I wanted something that can leverage Kotlin's fantastic support for so-called internal DSLs. Like Ruby, you
can create domain specific languages using Kotlin's language features. In Kotlin, this works with regular functions
that take a block of code as a parameter. If such a parameter is the last one in a function, you can move the block outside 
the parentheses. And if there are no other parameters those are optional. And then I realized that I could use 
reflection to figure exactly from where the function call is made. This became the core 
of what kotlin4example does. Any time you call example, it figures out from where in the code it is called and grabs the source 
code in the block. 

The library has a few other features, which are detailed in the examples below. But the simple idea is what
differentiates kotlin4example from other solutions. I'm not aware of any better or more convenient way to write 
documentation for Kotlin libraries.


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

The block you pass to example can be a suspending block. It uses `runBlocking` to run it. Earlier
versions of this library had a separate function for this; this is no longer needed.

### Configuring examples

Sometimes you just want to show but not run the code. You can control this with the 
`runExample` parameter.

```kotlin
//
example(
  runExample = false,
) {
  // your code goes here
}
```

The library imposes a line length of 80 characters on your examples. The 
reason is that code blocks with horizontal scroll bars look ugly. 

You can of course turn this off or turn on the built in wrapping (wraps at the 80th character) 

```kotlin

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
  // your code goes here
}
```

### Code snippets

While it is nice to have executable blocks, 
sometimes you just want to grab
code directly from a file. You can do that with snippets.

```kotlin
// the BEGIN_ and END_ are optional but I find it
// helps for readability.
// BEGIN_MY_CODE_SNIPPET
println("Example code that shows in a snippet")
// END_MY_CODE_SNIPPET
exampleFromSnippet("readme.kt", "MY_CODE_SNIPPET")
```

### Markdown

```kotlin
section("Section") {
  +"""
    You can use string literals, templates ${1 + 1}, 
    and [links](https://github.com/jillesvangurp/kotlin4example)
    or other markdown formatting.
  """.trimIndent()
}
// you can also just include markdown files
// useful if you have a lot of markdown
// content without code examples
includeMdFile("intro.md")
// link to things in your git repository
mdLink(DocGenTest::class)
mdLinkToRepoResource("build file", "build.gradle.kts")
mdLinkToSelf("This class")
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
    val readmePage = Page("Kotlin4Example", fileName = "README.md")
    // readmeMarkdown is a lazy of the markdown content
    readmePage.write(markdown = readmeMarkdown)
  }
}
```

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
project is where this project emerged from and all markdown in that project is generated by kotlin4example. Give it a 
try on one of your own projects and let me know what you think.


