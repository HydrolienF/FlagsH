import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish` // Add ./gradlew publishToMavenLocal
}

group="fr.formiko.flagsh"
version="4.0.1"
description="Display banners as flags."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.glaremasters.me/repository/towny/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.0.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
}

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
//   toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    shadowJar {
        relocate("org.bstats","${project.group}.bstats")
        relocate("co.aikar.commands","${project.group}.acf")
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17) // See https://openjdk.java.net/jeps/247 for more information.
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.20",
            "group" to project.group
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}