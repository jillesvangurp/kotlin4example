plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.ben-manes.versions") version "0.28.0" // gradle dependencyUpdates -Drevision=release
    `maven-publish`
}

val slf4jVersion = "1.7.26"
val junitVersion = "5.6.2"

group = "com.jillesvangurp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("io.github.microutils:kotlin-logging:1.8.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.1")

    // setup logging
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.13.3") // es seems to insist on log4j2
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val artifactName = "es-kotlin-wrapper-client"
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
