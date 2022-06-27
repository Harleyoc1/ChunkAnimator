import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseExtension
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import org.apache.tools.ant.filters.ReplaceTokens
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("net.minecraftforge.gradle")
    id("idea")
    id("maven-publish")
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("wtf.gofancy.fancygradle") version "1.1.2-0"
}

repositories {
    maven("https://maven.tehnut.info")
    maven("https://www.cursemaven.com")
    maven("https://maven.blamejared.com")
}

fun property(key: String): String = project.findProperty(key).toString()

fun optionalProperty(key: String) = project.findProperty(key)?.toString()

val modName = property("modName")
val modId = property("modId")
val modVersion = property("modVersion")

val mcVersion = property("mcVersion")
val forgeVersion = property("forgeVersion")
val mappingsVersion = property("mappingsVersion")

version = "$mcVersion-$modVersion"
group = property("group")

minecraft {
    mappings("snapshot", mappingsVersion)

    runs {
        create("client") {
            workingDirectory = file("run").absolutePath

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")

            if (project.hasProperty("mcUuid")) {
                args("--uuid", project.property("mcUuid"))
            }
            if (project.hasProperty("mcUsername")) {
                args("--username", project.property("mcUsername"))
            }
            if (project.hasProperty("mcAccessToken")) {
                args("--accessToken", project.property("mcAccessToken"))
            }
        }

        create("server") {
            workingDirectory = file("run").absolutePath

            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
        }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:${mcVersion}-${forgeVersion}")
}

fancyGradle {
    patches {
        resources
        coremods
        asm
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)

    from(sourceSets.main.get().resources.srcDirs) {
        include("mcmod.info")

        expand("version" to project.version, "mcversion" to mcVersion)
    }

    from(sourceSets.main.get().resources.srcDirs) {
        exclude("mcmod.info")
    }
}

// Assign version constant in ModConstants.
val prepareSources = tasks.register("prepareSources", Copy::class) {
    from("src/main/java")
    into("build/src/main/java")
    filter<ReplaceTokens>("tokens" to mapOf("VERSION" to version.toString()))
}

tasks.compileJava {
    source = prepareSources.get().outputs.files.asFileTree
}

tasks.jar {
    manifest.attributes(
            "Specification-Title" to project.name,
            "Specification-Vendor" to "lumien231",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "lumien231",
            "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    )

    archiveBaseName.set(modName)
    finalizedBy("reobfJar")
}

java {
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val deobfJar = tasks.register("deobfJar", Jar::class) {
    archiveClassifier.set("deobf")
    from(sourceSets.main.get().output)
}

tasks.build {
    dependsOn(deobfJar)
}

tasks.jar {
    manifest.attributes(
        "FMLCorePluginContainsFMLMod" to "true",
        "FMLCorePlugin" to "lumien.chunkanimator.asm.LoadingPlugin"
    )
}

curseforge {
    if (project.hasProperty("curseApiKey")) {
        apiKey = project.property("curseApiKey")

        project {
            id = "236484"

            addGameVersion(mcVersion)

            changelog = "Changelog added manually after release."
            changelogType = "markdown"
            releaseType = optionalProperty("curseFileType") ?: "release"

            addArtifact(tasks.findByName("sourcesJar"))
            addArtifact(deobfJar.get())
        }
    } else {
        project.logger.log(LogLevel.WARN, "API Key and file type for CurseForge not detected; uploading will be disabled.")
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "$modName-$mcVersion"
            version = modVersion

            from(components["java"])
            artifact(deobfJar.get())

            pom {
                name.set(modName)
                url.set("https://github.com/Harleyoc1/$modName")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org")
                    }
                }
                developers {
                    developer {
                        id.set("Harleyoc1")
                        name.set("Harley O'Connor")
                        email.set("Harleyoc1@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Harleyoc1/$modName.git")
                    developerConnection.set("scm:git:ssh://github.com/Harleyoc1/$modName.git")
                    url.set("https://github.com/Harleyoc1/$modName")
                }
            }

            pom.withXml {
                val element = asElement()

                // Clear dependencies.
                for (i in 0 until element.childNodes.length) {
                    val node = element.childNodes.item(i)
                    if (node?.nodeName == "dependencies") {
                        element.removeChild(node)
                    }
                }
            }
        }
    }
    repositories {
        maven("file:///${project.projectDir}/mcmodsrepo")
        if (project.hasProperty("harleyOConnorMavenUsername") &&
                project.hasProperty("harleyOConnorMavenPassword")) {
            maven("https://harleyoconnor.com/maven") {
                name = "HarleyOConnor"
                credentials {
                    username = project.property("harleyOConnorMavenUsername").toString()
                    password = project.property("harleyOConnorMavenPassword").toString()
                }
            }
        } else {
            logger.log(LogLevel.WARN, "Credentials for maven not detected; it will be disabled.")
        }
    }
}

tasks.register("publishToAllPlatforms") {
    this.dependsOn("publishMavenJavaPublicationToHarleyOConnorRepository", "curseforge")
}

// Extensions to make CurseGradle extension slightly neater.

fun CurseExtension.project(action: CurseProject.() -> Unit) {
    this.project(closureOf(action))
}

fun CurseProject.mainArtifact(artifact: Task?, action: CurseArtifact.() -> Unit) {
    this.mainArtifact(artifact, closureOf(action))
}

fun CurseArtifact.relations(action: CurseRelation.() -> Unit) {
    this.relations(closureOf(action))
}
