package com.jillesvangurp.kotlin4example

data class SourceRepository(
    // assumes a github repository for now that contains a kotlin project
    val repoUrl: String,
    val branch: String = "master",
    val sourcePaths: Set<String> = setOf("src/main/kotlin", "src/test/kotlin")) {

    fun md(block: Kotlin4Example.() -> Unit) = lazyOf(
        Kotlin4Example.markdown(this, block)
    )

    fun urlForFile(path: String) = "${repoUrl}/tree/${branch}/$path"
}
