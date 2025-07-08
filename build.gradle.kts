plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish` // Add ./gradlew publishToMavenLocal
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.sonarqube") version "5.0.0.4638"
    id("io.papermc.hangar-publish-plugin") version "0.1.3"
    id("com.modrinth.minotaur") version "2.+" // cf https://github.com/modrinth/minotaur
}

group="fr.formiko.flagsh"
version="4.4.6"
description="Display banners as flags."
val mainMinecraftVersion = "1.21.7"
val supportedMinecraftVersions = "1.20 - 1.21.7"
val townyVersion = "0.101.0.2"
val jacksonVersion = "2.18.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.glaremasters.me/repository/towny/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$mainMinecraftVersion-R0.1-SNAPSHOT")
    compileOnly("com.palmergames.bukkit.towny:towny:$townyVersion")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
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
            "com.fasterxml.jackson"
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
            github("TownyAdvanced", "Towny", "$townyVersion", "towny-$townyVersion.jar") // we can't use the latest release because it's inside a zip.
        }
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion(mainMinecraftVersion)
    }
    // runPaper.folia.registerTask()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.register("echoVersion") {
    doLast {
        println("${project.version}")
    }
}

tasks.register("echoReleaseName") {
    doLast {
        println("${project.version} [${supportedMinecraftVersions}]")
    }
}

val extractChangelog = tasks.register("extractChangelog") {
    group = "documentation"
    description = "Extracts the changelog for the current project version from CHANGELOG.md, including the version header."

    val changelog = project.objects.property(String::class)
    outputs.upToDateWhen { false }

    doLast {
        val version = project.version.toString()
        val changelogFile = project.file("CHANGELOG.md")

        if (!changelogFile.exists()) {
            println("CHANGELOG.md not found.")
            changelog.set("No changelog found.")
            return@doLast
        }

        val lines = changelogFile.readLines()
        val entries = mutableListOf<String>()
        var foundVersion = false

        for (line in lines) {
            when {
                // Include the version line itself
                line.trim().equals("# $version", ignoreCase = true) -> {
                    foundVersion = true
                    entries.add(line)
                }
                // Stop collecting at the next version header
                foundVersion && line.trim().startsWith("# ") -> break
                // Collect lines after the version header
                foundVersion -> entries.add(line)
            }
        }

        val result = if (entries.isEmpty()) {
            "Update to $version."
        } else {
            entries.joinToString("\n").trim()
        }

        // println("Changelog for version $version:\n$result")
        changelog.set(result)
    }

    // Make changelog accessible from other tasks
    extensions.add("changelog", changelog)
}

tasks.register("echoLatestVersionChangelog") {
    group = "documentation"
    description = "Displays the latest version change."

    dependsOn(tasks.named("extractChangelog"))

    doLast {
        println((extractChangelog.get().extensions.getByName("changelog") as Property<String>).get())
    }
}


val versionString: String = version as String
val isRelease: Boolean = !versionString.contains("SNAPSHOT")

hangarPublish { // ./gradlew publishPluginPublicationToHangar
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set(if (isRelease) "Release" else "Snapshot")
        id.set(project.name)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        changelog.set(
            extractChangelog.map {
                (it.extensions.getByName("changelog") as Property<String>).get()
            }
        )
        platforms {
            register(io.papermc.hangarpublishplugin.model.Platforms.PAPER) {
                url = "https://github.com/Mvndi/"+project.name+"/releases/download/"+versionString+"/"+project.name+"-"+versionString+".jar"

                // Set platform versions from gradle.properties file
                val versions: List<String> = supportedMinecraftVersions.replace(" ", "").split(",")
                platformVersions.set(versions)
            }
        }
    }
}

// Do an array of game versions from supportedMinecraftVersions
fun expandMinecraftVersions(range: String): List<String> {
    val latestPatches = mapOf("1.20" to 6, "1.21" to 6)

    fun String.toMinorAndPatch() = split('.').let {
        if (it.size == 2) it.joinToString(".") to 0 else "${it[0]}.${it[1]}" to it[2].toInt()
    }

    val (startMinor, startPatch) = range.split(" - ")[0].trim().toMinorAndPatch()
    val (endMinor, endPatch) = range.split(" - ")[1].trim().toMinorAndPatch()

    return generateSequence(startMinor) { current ->
        val (major, minor) = current.split('.').map { it.toInt() }
        if (current == endMinor) null else "%d.%d".format(major, minor + 1)
    }.flatMap { minor ->
        val from = if (minor == startMinor) startPatch else 0
        val to = if (minor == endMinor) endPatch else latestPatches[minor] ?: 0
        (from..to).map { if (it == 0) minor else "$minor.$it" }
    }.toList()
}

tasks.register("echoSupportedMinecraftVersions") {
    group = "documentation"
    description = "Displays the supported Minecraft versions."
    doLast {
        println("${expandMinecraftVersions(supportedMinecraftVersions).joinToString(", ")}")
    }
}


modrinth {
    token.set(System.getenv("MODRINTH_TOKEN")) // Remember to have the MODRINTH_TOKEN environment variable set or else this will fail - just make sure it stays private!
    projectId.set("bomemeallimiter") // This can be the project ID or the slug. Either will work!
    versionNumber.set("${project.version}") // You don't need to set this manually. Will fail if Modrinth has this version already
    versionType.set("release") // This is the default -- can also be `beta` or `alpha`
    uploadFile.set(layout.buildDirectory.dir("libs").get().asFile.absolutePath + "/${project.name}-${project.version}.jar")
    gameVersions.addAll(expandMinecraftVersions(supportedMinecraftVersions)) // Must be an array, even with only one version
    loaders.addAll("paper", "folia", "purpur", "spigot", "bukkit") // Must also be an array
    changelog.set(
        extractChangelog.map {
            (it.extensions.getByName("changelog") as Property<String>).get()
        }
    )
    syncBodyFrom = rootProject.file("README.md").readText()
}

tasks.named("modrinth") {
    dependsOn(tasks.named("modrinthSyncBody"))
}