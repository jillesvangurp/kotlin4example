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
