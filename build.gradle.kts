import com.matthewprenger.cursegradle.CurseProject
import java.time.Instant
import java.time.format.DateTimeFormatter

fun property(key: String) = project.findProperty(key).toString()
fun optionalProperty(key: String) = project.findProperty(key)?.toString()

apply(from = "https://gist.githubusercontent.com/Harleyoc1/4d23d4e991e868d98d548ac55832381e/raw/applesiliconfg.gradle")

plugins {
    id("java")
    id("net.neoforged.gradle.userdev") version "7.0.145"
    id("idea")
    id("maven-publish")
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.+"
}

tasks.named("wrapper", Wrapper::class).configure {
    distributionType = Wrapper.DistributionType.BIN
}

val modName = property("modName")
val modId = property("modId")
val modVersion = property("modVersion")

val mcVersion = property("mcVersion")

version = "$mcVersion-$modVersion"
group = property("group")

base {
    archivesName.set(modName)
}

runs {
    configureEach {
        systemProperty("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
        systemProperty("forge.logging.console.level", "debug")

        modSource(project.sourceSets["main"])
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${property("forgeVersion")}")
}

java {
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType(ProcessResources::class).configureEach {
    val replaceProperties = mapOf(
        "mcVersionRange" to project.findProperty("mcVersionRange"),
        "forgeVersionRange" to project.findProperty("forgeVersionRange"),
        "loaderVersionRange" to project.findProperty("loaderVersionRange"),
        "modId" to project.findProperty("modId"),
        "modVersion" to project.findProperty("modVersion")
    )
    inputs.properties(replaceProperties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
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
        "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    )

    archiveBaseName.set(modName)
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

curseforge {
    if (!project.hasProperty("curseApiKey")) {
        project.logger.warn("API Key for CurseForge not detected; uploading will be disabled.")
        return@curseforge
    }

    apiKey = property("curseApiKey")

    project(closureOf<CurseProject> {
        id = "236484"

        addGameVersion(mcVersion)

        changelog = "Changelog pending."
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
    changelog.set("Changelog pending.")
}
