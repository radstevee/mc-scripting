plugins {
    kotlin("jvm") version "2.0.20"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "net.radstevee"
version = "0.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("org.incendo:cloud-bukkit:2.0.0-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.1")
    }
}
