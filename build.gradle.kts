plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.1.build.+")
    implementation(project(":api"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(rootProject.version.toString())
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion("26.1.1")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    register("cleanBuild") {
        description = "Runs clean before building"
        group = "build"
        dependsOn(clean)
        finalizedBy(build)
    }
}
