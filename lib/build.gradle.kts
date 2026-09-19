plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    jacoco
    id("com.diffplug.spotless") version "8.7.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
}

mavenPublishing {
    coordinates("tech.jnkr", "presume", "0.1.1")

    pom {
        name.set("Presume")
        description.set("A library for property-based testing")
        inceptionYear.set("2026")
        url.set("https://github.com/josephjunker/presume/")
        licenses {
            license {
                name.set("The MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
            }
        }
        developers {
            developer {
                id.set("josephsoftwarequalityjunker@gmail.com")
                name.set("Joseph Junker")
                url.set("https://github.com/josephjunker/")
            }
        }
        scm {
            url.set("https://github.com/josephjunker/presume/")
            connection.set("scm:git:git://github.com/josephjunker/presume.git")
            developerConnection.set("scm:git:ssh://git@github.com/josephjunker/presume.git")
        }
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.15"
}

spotless {
    java {
        googleJavaFormat().style("AOSP").reorderImports(true).reflowLongStrings(true)
        formatAnnotations()
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()

    systemProperty("java.util.logging.config.file", "${layout.buildDirectory.get()}/resources/test/logging.properties")
    testLogging {
        showStandardStreams = true
    }

    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    mustRunAfter(tasks.named("test"))

    reports {
        html.required = true
        xml.required = true
        csv.required = false
    }
}

tasks.register("showCoverage") {
    group = "verification"
    description = "Open the test coverage report"
    dependsOn(tasks.named("jacocoTestReport"))

    val reportFile = layout.buildDirectory.file("reports/jacoco/test/html/index.html")

    doLast {
        val file = reportFile.get().asFile

        val command = listOf("open", file.absolutePath)
        ProcessBuilder(command).start()
    }
}