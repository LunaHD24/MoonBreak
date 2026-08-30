plugins {
    id("java-library")
    id("maven-publish")
}

group = "dev.lunaa.moonbreak"
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("io.papermc.paper:paper-api:26.1.1.build.+")
}

tasks {
    test {
        useJUnitPlatform()
    }

    jar {
        archiveBaseName.set("moonbreak-api")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "moonbreak-api"
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GithubPages"
            url = uri("${rootProject.projectDir}/build/gh-pages-repo")
        }
    }
}