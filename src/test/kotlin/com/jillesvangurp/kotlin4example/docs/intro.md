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

