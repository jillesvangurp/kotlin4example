package com.jillesvangurp.kotlin4example

/**
 * This is used to tell Kotlin4Example where to look for code in your project and to create
 * markdown links to files in your repository.
 */
data class SourceRepository(
    /** The link to your Github repository. */
    val repoUrl: String,
    /** The name of your primary branch. Defaults to main. */
    val branch: String = "main",
    /** Relative paths to all folders where Kotlin4Example should look for source files.*/
    val sourcePaths: Set<String> = setOf("src/main/kotlin", "src/test/kotlin")) {

    /**
     * Quick way to create markdown for your repository
     */
    fun md(block: Kotlin4Example.() -> Unit) = lazyOf(
        Kotlin4Example.markdown(this, block)
    )

    /**
     * Construct a url to a given path in your repository.
     */
    fun urlForFile(path: String) = "${repoUrl}/tree/${branch}/$path"
}
