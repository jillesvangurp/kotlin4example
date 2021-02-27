plugins {
    kotlin("jvm") version "1.4.31"
    id("com.github.ben-manes.versions") version "0.36.0" // gradle dependencyUpdates -Drevision=release
    `maven-publish`
}

val slf4jVersion = "1.7.30"
val junitVersion = "5.7.0"

group = "com.jillesvangurp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("io.github.microutils:kotlin-logging:2.0.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.kotest:kotest-assertions-core:4.4.1")

    // setup logging
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.14.0") // es seems to insist on log4j2
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

val artifactName = "kotlin4example"
val artifactGroup = "com.github.jillesvangurp"

publishing {
    publications {
        create<MavenPublication>("lib") {
            groupId = artifactGroup
            artifactId = artifactName
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "myRepo"
            url = uri("file://$buildDir/repo")
        }
    }
}
