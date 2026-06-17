import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    id("dev.architectury.loom") version "1.17-SNAPSHOT"
    id("systems.manifold.manifold-gradle-plugin") version "0.0.2-alpha"
}

val mcVersion = "1.21.6"
val loader = (findProperty("loom.platform") as String?)?.lowercase() ?: "fabric"
val isFabric = loader == "fabric"
val isNeoForge = loader == "neoforge"

require(isFabric || isNeoForge) {
    "Unsupported loom.platform '$loader'. Use 'fabric' or 'neoforge'."
}

fun projectProp(key: String): String = property(key).toString()

group = projectProp("mod.group")
version = projectProp("mod.version")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.terraformersmc.com/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

loom {
    runConfigs.configureEach {
        runDir = "run/$loader"
    }
    mixin {
        useLegacyMixinAp.set(true)
        defaultRefmapName.set("cerulean.refmap.json")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    if (isFabric) {
        modImplementation("net.fabricmc:fabric-loader:0.16.13")
        modImplementation("net.fabricmc.fabric-api:fabric-api:0.128.2+1.21.6")
        compileOnly("com.terraformersmc:modmenu:14.0.0-rc.2")
    } else {
        add("neoForge", "net.neoforged:neoforge:21.6.20-beta")
    }

    compileOnly("systems.manifold:manifold-preprocessor:2025.1.24")
    annotationProcessor("systems.manifold:manifold-preprocessor:2025.1.24")
}

val generatedMainRoot = layout.buildDirectory.dir("generated/$loader/main")
val generatedJavaDir = layout.buildDirectory.dir("generated/$loader/main/java")
val generatedResourcesDir = layout.buildDirectory.dir("generated/$loader/main/resources")

val prepareLoaderSources = tasks.register("prepareLoaderSources") {
    inputs.dir("src/main/java")
    inputs.dir("src/main/resources")
    outputs.dir(generatedMainRoot)

    doLast {
        val root = generatedMainRoot.get().asFile
        val javaOut = generatedJavaDir.get().asFile
        val resourcesOut = generatedResourcesDir.get().asFile

        delete(root)
        copy {
            from("src/main/java")
            exclude("toni/cerulean/foundation/data/**")
            into(javaOut)
        }
        copy {
            from("src/main/resources")
            into(resourcesOut)
        }

        if (isFabric) {
            delete(
                resourcesOut.resolve("META-INF/mods.toml"),
                resourcesOut.resolve("META-INF/neoforge.mods.toml")
            )
        } else {
            delete(resourcesOut.resolve("fabric.mod.json"))
        }

        val props = mutableListOf(
            "MC=211",
            "mc=211"
        )

        if (isFabric) {
            props += listOf("FABRIC=", "fabric=")
        } else {
            props += listOf("NEO=", "neo=", "FORGELIKE=", "forgelike=")
        }

        root.resolve("build.properties").writeText(props.joinToString(System.lineSeparator(), postfix = System.lineSeparator()))
    }
}

sourceSets.named("main") {
    java.setSrcDirs(listOf(generatedJavaDir))
    resources.setSrcDirs(listOf(generatedResourcesDir))
}

val expandProperties = mapOf(
    "id" to projectProp("mod.id"),
    "name" to projectProp("mod.name"),
    "namespace" to projectProp("mod.namespace"),
    "group" to projectProp("mod.group"),
    "display_name" to projectProp("mod.display_name"),
    "description" to projectProp("mod.description"),
    "license" to projectProp("mod.license"),
    "modversion" to version.toString(),
    "mixinid" to projectProp("mod.id"),
    "discord" to projectProp("mod.discord"),
    "depends" to ""
)

tasks.withType<JavaCompile>().configureEach {
    dependsOn(prepareLoaderSources)
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    dependsOn(prepareLoaderSources)
    filteringCharset = "UTF-8"
    inputs.properties(expandProperties)
    filesMatching(
        listOf(
            "fabric.mod.json",
            "META-INF/mods.toml",
            "META-INF/neoforge.mods.toml"
        )
    ) {
        expand(expandProperties)
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    archiveBaseName.set("${projectProp("mod.id")}-$loader")
    archiveVersion.set("${project.version}-$mcVersion")
}

tasks.withType<Jar>().configureEach {
    dependsOn(prepareLoaderSources)
}

val gradleWrapper = if (System.getProperty("os.name").startsWith("Windows")) "gradlew.bat" else "./gradlew"
val isWindows = System.getProperty("os.name").startsWith("Windows")

tasks.register<Exec>("buildFabric") {
    group = "build"
    description = "Build Cerulean for Fabric 1.21.6"
    workingDir = rootDir
    if (isWindows) {
        commandLine("cmd", "/c", gradleWrapper, "build", "-Ploom.platform=fabric", "--no-daemon")
    } else {
        commandLine(gradleWrapper, "build", "-Ploom.platform=fabric", "--no-daemon")
    }
}

tasks.register<Exec>("buildNeoForge") {
    group = "build"
    description = "Build Cerulean for NeoForge 1.21.6"
    workingDir = rootDir
    mustRunAfter("buildFabric")
    if (isWindows) {
        commandLine("cmd", "/c", gradleWrapper, "build", "-Ploom.platform=neoforge", "--no-daemon")
    } else {
        commandLine(gradleWrapper, "build", "-Ploom.platform=neoforge", "--no-daemon")
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Build Cerulean for both Fabric and NeoForge"
    dependsOn("buildFabric", "buildNeoForge")
}
