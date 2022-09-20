/// build.gradle.kts (ManifestPlugin):
/// =================================
///
/// Access gradle.properties:
///     yes -> "val prop_name = project.extra['prop.name']"
///     no  -> "val prop_name = property('prop.name')"

/** 1) Plugins used globally */
plugins {
    jacoco

    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`

    id("org.jetbrains.kotlin.jvm") version "1.4.20"
    id("com.gradle.plugin-publish") version "1.0.0"
    id("org.sonarqube") version "3.4.0.2513"
}


/** 2) Apply script plugin to handle publishing */
apply(from = "gradle/Publishing.gradle")


/** 3) General information regarding the plugin */
group   = project.extra["plugin.group"]!! as String
version = (project.extra["plugin.version"]!! as String).replace("-pre", "")


/** 4) Dependency source configuration */
repositories {
    mavenCentral()
    gradlePluginPortal()
}


/** 5) Plugin dependencies */
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-bom")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("com.github.stefanbirkner:system-lambda:1.2.1")
    testImplementation(gradleTestKit())
}


/** 6) JaCoCo configuration */
jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}


/** 7) Gradle test configuration */
tasks.withType<Test> {
    ignoreFailures = true
    testLogging.showStandardStreams = true
}


/** 8) Plugin configuration */
pluginBundle {
    website = project.extra["plugin.url"]!! as String
    vcsUrl  = project.extra["plugin.git"]!! as String
    tags    = (project.extra["plugin.tags"]!! as String).split(",")
}


/** 9) Configuration for publishing plugin to Gradle Plugin Portal */
gradlePlugin {
    plugins {
        create(project.extra["plugin.name"]!! as String) {
            id                  = project.extra["plugin.id"]!! as String
            displayName         = project.extra["plugin.displayName"]!! as String
            description         = project.extra["plugin.description"]!! as String
            implementationClass = project.extra["plugin.class"]!! as String
        }
    }
}


/** 10) publish plugin to local Maven repository for integration test after creating JAR */
tasks.jar.get().finalizedBy(tasks.getByName("publishToMavenLocal"))
