import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0-Beta2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "2.0.0-Beta2"
}

group = "one.devsky.boilerplates"
version = "1.0-SNAPSHOT"

val jdaVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project

repositories {
    mavenCentral()
    maven("https://nexus.flawcra.cc/repository/mirrors/")
    maven("https://repo.fruxz.dev/releases/")
}

val shadowDependencies = listOf(
    // Logging
    "ch.qos.logback:logback-classic:1.5.3",

    "net.dv8tion:JDA:$jdaVersion",

    "dev.fruxz:ascend:2024.1.1",
    "io.github.cdimascio:dotenv-kotlin:6.4.1",
    "com.google.code.gson:gson:2.10.1",

    // Utils
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2",
    "net.oneandone.reflections8:reflections8:0.11.7",

    // Database
    "org.jetbrains.exposed:exposed-core:$exposedVersion",
    "org.jetbrains.exposed:exposed-dao:$exposedVersion",
    "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
    "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
    "com.mysql:mysql-connector-j:8.3.0",
    "org.mariadb.jdbc:mariadb-java-client:3.3.3",
    "com.zaxxer:HikariCP:5.1.0",

    // Ktor (HTTP Client)
    "io.ktor:ktor-client-core:$ktorVersion",
    "io.ktor:ktor-client-cio:$ktorVersion",
    "io.ktor:ktor-client-content-negotiation:$ktorVersion",
    "io.ktor:ktor-serialization-kotlinx-json:$ktorVersion",
)

dependencies {
    // Ktor (HTTP Client)
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")


    testImplementation(kotlin("test"))

    shadowDependencies.forEach { dependency ->
        implementation(dependency)
        shadow(dependency)
    }
}

tasks {

    build {
        dependsOn("shadowJar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("${project.name}.jar")

        manifest {
            attributes["Main-Class"] = "one.devsky.boilerplates.StartKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}