plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish` // Add ./gradlew publishToMavenLocal
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.sonarqube") version "5.0.0.4638"
}

group="fr.formiko.flagsh"
version="4.4.0"
description="Display banners as flags."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.glaremasters.me/repository/towny/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.palmergames.bukkit.towny:towny:0.101.0.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        val prefix = "${project.group}.lib"
        sequenceOf(
            "co.aikar",
            "org.bstats",
            "com.fasterxml.jackson.core"
        ).forEach { pkg ->
            relocate(pkg, "$prefix.$pkg")
        }

        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    assemble {
        dependsOn(shadowJar)
    }
    processResources {
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
    runServer {
        downloadPlugins {
            github("TownyAdvanced", "Towny", "0.101.0.2", "towny-0.101.0.2.jar") // we can't use the latest release because it's inside a zip.
        }
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
    }
    // runPaper.folia.registerTask()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}