import com.google.gson.Gson
import com.google.gson.JsonObject
import com.matthewprenger.cursegradle.CurseProject
import net.minecraftforge.gradle.common.util.RunConfig
import java.io.InputStreamReader
import java.time.Instant
import java.time.format.DateTimeFormatter

fun property(key: String) = project.findProperty(key).toString()
fun optionalProperty(key: String) = project.findProperty(key)?.toString()

apply(from = "https://gist.githubusercontent.com/Harleyoc1/4d23d4e991e868d98d548ac55832381e/raw/applesiliconfg.gradle")

plugins {
    id("java")
    id("net.minecraftforge.gradle")
    id("org.parchmentmc.librarian.forgegradle")
    id("org.spongepowered.mixin")
    id("idea")
    id("maven-publish")
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.+"
}

val modName = property("modName")
val modId = property("modId")
val modVersion = property("modVersion")

val mcVersion = property("mcVersion")

version = "$mcVersion-$modVersion"
group = property("group")

minecraft {
    mappings("parchment", "${property("mappingsVersion")}-$mcVersion")
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        create("client") {
            workingDirectory = file("run").absolutePath

            this.applyDefaultConfiguration()

            if (project.hasProperty("mcUuid")) {
                args("--uuid", property("mcUuid"))
            }
            if (project.hasProperty("mcUsername")) {
                args("--username", property("mcUsername"))
            }
            if (project.hasProperty("mcAccessToken")) {
                args("--accessToken", property("mcAccessToken"))
            }
        }

        create("server") {
            workingDirectory = file("run-server").absolutePath

            this.applyDefaultConfiguration()
        }
    }
}

mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
}

dependencies {
    minecraft("net.minecraftforge:forge:$mcVersion-${property("forgeVersion")}")

    annotationProcessor("org.spongepowered:mixin:${property("mixinVersion")}:processor")
}

java {
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.jar {
    manifest.attributes(
        "Specification-Title" to project.name,
        "Specification-Vendor" to "lumien231", // lumien231 was the original author of this mod.
        "Specification-Version" to project.version,
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "Harley O'Connor", // I am the author of this port.
        "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
        "MixinConnector" to "lumien.chunkanimator.MixinConnector"
    )

    archiveBaseName.set(modName)
    finalizedBy("reobfJar")
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
                        email.set("harleyoc1@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Harleyoc1/$modName.git")
                    developerConnection.set("scm:git:ssh://github.com/Harleyoc1/$modName.git")
                    url.set("https://github.com/Harleyoc1/$modName")
                }
            }
        }
    }
    repositories {
        maven("file:///${project.projectDir}/mcmodsrepo")
        if (hasProperty("harleyOConnorMavenUsername") && hasProperty("harleyOConnorMavenPassword")) {
            maven("https://harleyoconnor.com/maven") {
                name = "HarleyOConnor"
                credentials {
                    username = property("harleyOConnorMavenUsername")
                    password = property("harleyOConnorMavenPassword")
                }
            }
        } else {
            logger.log(LogLevel.WARN, "Credentials for maven not detected; it will be disabled.")
        }
    }
}

fun readRawChangelog(): String? {
    val versionInfoFile = file("version_info.json")
    val jsonObject = Gson().fromJson(InputStreamReader(versionInfoFile.inputStream()), JsonObject::class.java)
    return jsonObject
        ?.get(mcVersion)?.asJsonObject
        ?.get(project.version.toString())?.asString
}

val rawChangelog = readRawChangelog()!!

curseforge {
    if (!project.hasProperty("curseApiKey")) {
        project.logger.warn("API Key for CurseForge not detected; uploading will be disabled.")
        return@curseforge
    }

    apiKey = property("curseApiKey")

    project(closureOf<CurseProject> {
        id = "236484"

        addGameVersion(mcVersion)

        changelog = rawChangelog
        changelogType = "markdown"
        releaseType = optionalProperty("versionType") ?: "release"

        addArtifact(tasks.findByName("sourcesJar"))
    })
}

modrinth {
    if (!project.hasProperty("modrinthToken")) {
        project.logger.warn("Token for Modrinth not detected; uploading will be disabled.")
        return@modrinth
    }

    token.set(property("modrinthToken"))
    projectId.set(modId)
    versionNumber.set("$mcVersion-$modVersion")
    versionType.set(optionalProperty("versionType") ?: "release")
    uploadFile.set(tasks.jar.get())
    gameVersions.add(mcVersion)
    changelog.set(rawChangelog)
}

fun RunConfig.applyDefaultConfiguration() {
    property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
    property("forge.logging.console.level", "debug")

    property("mixin.debug.export", "true")
    property("mixin.env.remapRefMap", "true")
    property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")

    args("-mixin.config=$modId.mixins.json")

    mods {
        create(modId) {
            source(sourceSets.main.get())
        }
    }
}