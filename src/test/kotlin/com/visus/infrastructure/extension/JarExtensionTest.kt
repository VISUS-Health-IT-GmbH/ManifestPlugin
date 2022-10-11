/*  JarExtensionTest.kt
 *
 *  Copyright (C) 2022, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.infrastructure.extension

import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.zip.ZipInputStream

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test


/**
 *  JarExtensionTest:
 *  ================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the Jar extension methods defined
 */
open class JarExtensionTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // test temporary directories
        private val projectProjectDir1  = File(buildDir, "${JarExtensionTest::class.simpleName}_1")
        private val projectBuildDir1    = File(projectProjectDir1, "build")
        private val projectLibsDir1     = File(projectBuildDir1, "libs")

        private val projectProjectDir2  = File(buildDir, "${JarExtensionTest::class.simpleName}_2")
        private val projectBuildDir2    = File(projectProjectDir2, "build")
        private val projectLibsDir2     = File(projectBuildDir2, "libs")


        /** 0) Create temporary directories for tests */
        @BeforeClass @JvmStatic fun configureTestsuite() {
            // i) remove directories if exists
            listOf(
                projectProjectDir1, projectProjectDir2
            ).forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    Files.walk(dir.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map { it.toFile() }
                        .forEach { it.delete() }
                }
            }

            // ii) create directories
            listOf(
                projectLibsDir1, projectLibsDir2
            ).forEach { it.mkdirs() }
        }
    }


    /** 1) Check the "Jar.patchManifest" method: no JAR archive artifact build only logging */
    @Test fun test_patchManifest_jarNotBuilt() {
        val project = ProjectBuilder.builder().build()

        // apply Java plugin & get JAR archive task
        project.pluginManager.apply(JavaPlugin::class.java)
        val task = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar

        // set listener to evaluate output logged by plugin
        project.logging.addStandardOutputListener { message ->
            Assert.assertTrue(message.contains("-> Jar.patchManifest : WARNING] The jar archive artifact at"))
            Assert.assertTrue(
                message.contains("does not exist, therefore skipping the patch process for this jar archive artifact!")
            )
        }

        // emulate patching the JAR archive artifact
        task.patchManifest(mapOf())
    }


    /** 2) Check the "Jar.patchManifest" method: JAR archive artifact build */
    @Test fun test_patchManifest_jarBuilt() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir1).build()

        // apply Java plugin & get JAR archive task
        project.pluginManager.apply(JavaPlugin::class.java)
        val task = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar

        // emulate running task actions when task is called
        task.actions.forEach {
            it.execute(task)
        }

        // emulate patching the JAR archive artifact
        task.patchManifest(mapOf("Test123" to "Test456"))
    }


    /** 3) Check the "Jar.patchManifest" method: evaluate patched manifest attributes */
    @Test fun test_Evaluate_patchedManifestAttributes() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir2).build()

        // apply Java plugin & get JAR archive task
        project.pluginManager.apply(JavaPlugin::class.java)
        val task = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar

        // emulate running task actions when task is called
        task.actions.forEach {
            it.execute(task)
        }

        // check not yet patched JAR archive artefact manifest file
        var content = project.file("${task.destinationDirectory.asFile.get()}/${task.archiveFileName.get()}")
                        .getManifestFileContent()
        Assert.assertFalse(content.contains("Test123: Test456"))

        // emulate patching the JAR archive artifact
        task.patchManifest(mapOf("Test123" to "Test456"))

        // check patched JAR archive artefact manifest file
        content = project.file("${task.destinationDirectory.asFile.get()}/${task.archiveFileName.get()}")
                    .getManifestFileContent()
        Assert.assertTrue(content.contains("Test123: Test456"))
    }
}


/**
 *  Simple hack to read the content of the META-INF/MANIFEST.MF from a JAR archive artefact
 *
 *  @return the manifest content as string
 */
internal fun File.getManifestFileContent() = ZipInputStream(FileInputStream(this)).use { zip ->
    generateSequence { zip.nextEntry }
        .filterNot { it.isDirectory }
        .filter { it.name == "META-INF/MANIFEST.MF" }
        .map { zip.readAllBytes() }
        .first().toString(Charsets.UTF_8)
}
