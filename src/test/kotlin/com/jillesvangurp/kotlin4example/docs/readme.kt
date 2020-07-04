package com.jillesvangurp.kotlin4example.docs

import com.jillesvangurp.kotlin4example.SourceRepository

val k4ERepo = SourceRepository("https://github.com/jillesvangurp/kotlin4example")

val readme by k4ERepo.md {
    +"""
        
        [![](https://jitpack.io/v/jillesvangurp/kotlin4example.svg)](https://jitpack.io/#jillesvangurp/kotlin4example)
        [![Actions Status](https://github.com/jillesvangurp/kotlin4example/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/kotlin4example/actions)
        
        Kotlin4Example is a project that I wrote to solve a recurring problem: I have a bunch of kotlin projects
        on Github that I would like to document properly. There are some tools for grabbing snippets of code
        from source files and templating markdown files. Usually this works by putting some strings in comments in 
        your code and using some tool to dig out code snippets from the source code. 

        There's nothing wrong with that approach. However, I wanted more. I want to actually run the snippets, 
        grab the output, and generate documentation using the Github flavor of markdown. Also, I did not want to deal
        with keeping track of snippet ids, their code comments, etc.
        
        So, I wrote a little kotlin framework to do this for me and used it to document a project. Over time, it
        gained a critical mass of features and convenience and I sat down to clean up the code base, add tests, etc.
        
        ## How Does it work?
        
        Kotlin has multi line strings, templating, and some built in constructions for creating your own DSLs. So, I
        created a simple DSL that generates markdown by concatenating strings or executable blocks. The executable 
        blocks as markdown source code blocks. We can also execute grab the output (optional). 
        
        This works by looking at the stacktrace and figuring out the path to the source file, the line of code 
        and figuring out the beginning and end of the block. 
        
        ## Example

        Here's a Hello World example:

    """

    blockWithOutput {
        println("Hello World")
    }

    +"""
        As you can see, we indeed grabbed the output.
        
        Check out the kotlin source code that generates this markdown ${mdLinkToSelf("here")}.
        
        ## Development status
        
        This is still a work in progress but it's also the basis for documentation for a few projects I maintain.
        
        So, API stability is at this point important for me. Which means, it should be fine for you as well.
    """
}
