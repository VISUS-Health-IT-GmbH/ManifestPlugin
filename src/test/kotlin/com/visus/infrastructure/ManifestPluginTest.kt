/*  ManifestPluginTest.kt
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
package com.visus.infrastructure

import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.GradleVersion

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties
import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

import com.visus.infrastructure.exception.JavaPluginMissingException
import com.visus.infrastructure.extension.getManifestFileContent


/**
 *  ManifestPluginTest:
 *  ==================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the ManifestPlugin class
 */
open class ManifestPluginTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // test temporary directories
        private val projectProjectDir1  = File(buildDir, "${ManifestPluginTest::class.simpleName}_1")
        private val projectBuildDir1    = File(projectProjectDir1, "build")
        private val projectLibsDir1     = File(projectBuildDir1, "libs")

        private val projectProjectDir2  = File(buildDir, "${ManifestPluginTest::class.simpleName}_2")
        private val projectBuildDir2    = File(projectProjectDir2, "build")
        private val projectLibsDir2     = File(projectBuildDir2, "libs")

        private val projectProjectDir3  = File(buildDir, "${ManifestPluginTest::class.simpleName}_3")
        private val projectBuildDir3    = File(projectProjectDir3, "build")
        private val projectLibsDir3     = File(projectBuildDir3, "libs")


        /** 0) Create temporary directories for tests */
        @BeforeClass
        @JvmStatic fun configureTestsuite() {
            // i) remove directories if exists
            if (projectProjectDir1.exists() && projectProjectDir1.isDirectory) {
                Files.walk(projectProjectDir1.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            if (projectProjectDir2.exists() && projectProjectDir2.isDirectory) {
                Files.walk(projectProjectDir2.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            if (projectProjectDir3.exists() && projectProjectDir3.isDirectory) {
                Files.walk(projectProjectDir3.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            // ii) create directories
            projectProjectDir1.mkdirs()
            projectBuildDir1.mkdirs()
            projectLibsDir1.mkdirs()

            projectProjectDir2.mkdirs()
            projectBuildDir2.mkdirs()
            projectLibsDir2.mkdirs()

            projectProjectDir3.mkdirs()
            projectBuildDir3.mkdirs()
            projectLibsDir3.mkdirs()
        }
    }


    /** 1) Test applying the plugin with JavaPlugin not applied */
    @Test fun test_ApplyPlugin_JavaPluginMissing() {
        val project = ProjectBuilder.builder().build()

        try {
            // try applying plugin (should fail) & evaluate
            project.pluginManager.apply(ManifestPlugin::class.java)
            project.evaluate()
        } catch (e: Exception) {
            // assert applying did not work because of no Java plugin applied
            // INFO: Equal to check on com.visus.infrastructure.exception.ManifestPluginException /
            //       org.gradle.api.InvalidUserDataException as it is based on it!
            Assert.assertTrue(e.cause is JavaPluginMissingException)
        }

        Assert.assertFalse(project.plugins.hasPlugin(ManifestPlugin::class.java))
    }


    /** 2) Test applying the plugin with JavaPlugin applied but nothing else */
    @Test fun test_ApplyPlugin_JavaPluginNothingElse() {
        val project = ProjectBuilder.builder().build()

        // apply JavaPlugin required
        project.pluginManager.apply(JavaPlugin::class.java)

        // apply ManifestPlugin & evaluate
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()
    }


    /** 3) Test applying the plugin with JavaPlugin / WarPlugin applied */
    @Test fun test_ApplyPlugin_JavaPluginWarPlugin() {
        val project = ProjectBuilder.builder().build()

        // apply JavaPlugin required
        project.pluginManager.apply(JavaPlugin::class.java)

        // apply optional WarPlugin
        project.pluginManager.apply(WarPlugin::class.java)

        // apply ManifestPlugin & evaluate
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()
    }


    /** 4) Evaluate the default properties set when nothing is available */
    @Test fun test_Evaluate_defaultProperties() {
        val project = ProjectBuilder.builder().build()

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertTrue(attributes.size >= 7)
        Assert.assertTrue(attributes.containsKey("Manifest-Version"))
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.GradleVersion))
        Assert.assertEquals(GradleVersion.current().version, attributes[ManifestPlugin.GradleVersion])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.CreatedBy))
        Assert.assertEquals("${System.getProperty("java.runtime.version")} (${System.getProperty("java.vendor")})",
                            attributes[ManifestPlugin.CreatedBy])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.Permissions))
        Assert.assertEquals("all-permissions", attributes[ManifestPlugin.Permissions])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.Codebase))
        Assert.assertEquals("*", attributes[ManifestPlugin.Codebase])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.ApplicationName))
        Assert.assertEquals(project.name, attributes[ManifestPlugin.ApplicationName])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_PRODUCT_NAME))
        Assert.assertEquals(project.name, attributes[ManifestPlugin.PROP_PRODUCT_NAME])
    }


    /** 5) Evaluate the default properties set when only custom version available */
    @Test fun test_Evaluate_customVersion() {
        val project = ProjectBuilder.builder().build().also { it.version = GradleVersion.current().version }

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_PRODUCT_VERSION))
        Assert.assertEquals(GradleVersion.current().version, attributes[ManifestPlugin.PROP_PRODUCT_VERSION])
    }


    /** 6) Evaluate the custom properties of gradle.properties including replacement of referenced properties */
    @Test fun test_Evaluate_customProperties() {
        val project = ProjectBuilder.builder().build()

        // emulate gradle.properties
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set("Test-Property-Key", "Test-Property-Value")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}PROP_USER_NAME", "hahnen")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}PROP_USER_BIRTHDATE", "11.07.1998")
        propertiesExtension.set(
            "${ManifestPlugin.PREFIX_DEFAULT}PROP_USER_INFO", "\${PROP_USER_NAME}/\${PROP_USER_BIRTHDATE}"
        )
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}PROP_TEST", "abc \${PROP_BUILD_HOST} def")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}PROP_TEST2", "abc \${PROP_Schnitzel} def")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_RELEASED}", false)

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertTrue(attributes.containsKey("PROP_USER_NAME"))
        Assert.assertEquals("hahnen", attributes["PROP_USER_NAME"])
        Assert.assertTrue(attributes.containsKey("PROP_USER_BIRTHDATE"))
        Assert.assertEquals("11.07.1998", attributes["PROP_USER_BIRTHDATE"])
        Assert.assertTrue(attributes.containsKey("PROP_USER_INFO"))
        Assert.assertEquals("hahnen/11.07.1998", attributes["PROP_USER_INFO"])
        Assert.assertTrue(attributes.containsKey("PROP_TEST"))
        Assert.assertEquals("abc ${InetAddress.getLocalHost().hostName} def", attributes["PROP_TEST"])
        Assert.assertTrue(attributes.containsKey("PROP_TEST2"))
        Assert.assertEquals("abc \${PROP_Schnitzel} def", attributes["PROP_TEST2"])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_PRODUCT_RELEASED))
        Assert.assertEquals("false", attributes[ManifestPlugin.PROP_PRODUCT_RELEASED])
    }


    /** 7) Evaluate the properties only set when project released */
    @Test fun test_Evaluate_releasedProperties() {
        val project = ProjectBuilder.builder().build()

        // emulate gradle.properties
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_RELEASED}", true)

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_PRODUCT_RELEASED))
        Assert.assertEquals("true", attributes[ManifestPlugin.PROP_PRODUCT_RELEASED])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_RELEASE_DATE))
        Assert.assertEquals(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            attributes[ManifestPlugin.PROP_RELEASE_DATE])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_RELEASE_DATE_yyMMdd))
        Assert.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")),
                            attributes[ManifestPlugin.PROP_RELEASE_DATE_yyMMdd])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_BUILD_USER))
        Assert.assertEquals(System.getProperty("user.name"), attributes[ManifestPlugin.PROP_BUILD_USER])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_BUILD_HOST))
        Assert.assertEquals(InetAddress.getLocalHost().hostName, attributes[ManifestPlugin.PROP_BUILD_HOST])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_BUILD_TIME))
    }


    /** 8) Evaluate using project extension and overwritten properties from gradle.properties */
    @Test fun test_Evaluate_ExtensionOverwrittenProperties() {
        val project = ProjectBuilder.builder().build()

        // emulate project extension & gradle.properties
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(
            project.name, mapOf(
                "version" to "1.2.3.4",
                "rc" to "RC01",
                "released" to false,
                "udi_eu" to "0123456789abcdef",
                "udi_usa" to "fedcba9876543210",
                "vendor" to "VISUS Health IT GmbH"
            )
        )
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_RC}", "RC02")
        propertiesExtension.set(
            "${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.MainClass}", "com.visus.infrastructure.Main"
        )

        // apply JavaPlugin (required) / WarPlugin (optional) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(WarPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val jarAttributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertFalse(jarAttributes.containsKey(ManifestPlugin.PROP_PRODUCT_VERSION))
        Assert.assertTrue(jarAttributes.containsKey(ManifestPlugin.PROP_PRODUCT_RC))
        Assert.assertEquals("RC02", jarAttributes[ManifestPlugin.PROP_PRODUCT_RC])
        Assert.assertTrue(jarAttributes.containsKey(ManifestPlugin.MainClass))
        Assert.assertEquals("com.visus.infrastructure.Main", jarAttributes[ManifestPlugin.MainClass])
        Assert.assertTrue(jarAttributes.containsKey(ManifestPlugin.PROP_PRODUCT_RELEASED))
        Assert.assertEquals("false", jarAttributes[ManifestPlugin.PROP_PRODUCT_RELEASED])
        Assert.assertTrue(jarAttributes.containsKey(ManifestPlugin.PROP_UNIQUE_DEVICE_IDENTIFICATION_EU))
        Assert.assertEquals("0123456789abcdef", jarAttributes[ManifestPlugin.PROP_UNIQUE_DEVICE_IDENTIFICATION_EU])
        Assert.assertTrue(jarAttributes.containsKey(ManifestPlugin.PROP_UNIQUE_DEVICE_IDENTIFICATION_USA))
        Assert.assertEquals("fedcba9876543210", jarAttributes[ManifestPlugin.PROP_UNIQUE_DEVICE_IDENTIFICATION_USA])
        Assert.assertTrue(jarAttributes.containsKey(ManifestPlugin.PROP_VENDOR_NAME))
        Assert.assertEquals("VISUS Health IT GmbH", jarAttributes[ManifestPlugin.PROP_VENDOR_NAME])

        // get main War task attributes
        val warAttributes = (project.tasks.getByName(WarPlugin.WAR_TASK_NAME) as War).manifest.attributes
        Assert.assertFalse(warAttributes.containsKey(ManifestPlugin.MainClass))
    }


    /** 9) Evaluate using different project extension name */
    @Test fun test_Evaluate_DifferentExtensionName() {
        val project = ProjectBuilder.builder().build()

        // emulate project extension & gradle.properties
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(
            "differentName", mapOf(
                "version" to "1.2.3.4"
            )
        )
        propertiesExtension.set(ManifestPlugin.KEY_EXTENSION, "differentName")

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_PRODUCT_VERSION))
        Assert.assertEquals("1.2.3.4", attributes[ManifestPlugin.PROP_PRODUCT_VERSION])
    }


    /** 10) Evaluate overwriting "Permissions" and "Codebase" attributes */
    @Test fun test_Evaluate_OverwritePermissionsCodebase() {
        val project = ProjectBuilder.builder().build()

        // emulate gradle.properties
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.Permissions}", "")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.Codebase}", "")

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task attributes
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertFalse(attributes.containsKey(ManifestPlugin.Permissions))
        Assert.assertFalse(attributes.containsKey(ManifestPlugin.Codebase))
    }


    /** 11) Evaluate applying the plugin, running JAR (but not WAR) task and patching all the artifacts afterwards */
    @Test fun test_Evaluate_PatchJarManifestAttributes() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir1).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_PATCHED}Resources-Project-Version", "\${PROP_PRODUCT_VERSION}")

        restoreSystemProperties {
            System.setProperty(ManifestPlugin.SYS_TICKET, "VISUS-1234")

            // apply JavaPlugin (required) / ManifestPlugin & evaluate
            project.pluginManager.apply(JavaPlugin::class.java)
            project.pluginManager.apply(ManifestPlugin::class.java)
            project.evaluate()

            // get main Jar task & emulate running task
            val task = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar
            task.actions.forEach {
                it.execute(task)
            }

            // check not yet patched JAR archive artefact manifest file
            var content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertFalse(content.contains("Resources-Project-Version: "))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertTrue(
                content.contains(
                    "Resources-Project-Version: ${project.version}.${System.getProperty(ManifestPlugin.SYS_TICKET)}"
                )
            )
        }
    }


    /** 12) Evaluate applying the plugin, running WAR (but not JAR) task but patching no version property */
    @Test fun test_Evaluate_PatchWarManifestAttributes() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir1).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_PATCHED}Resources-Project-Version", "\${PROP_PRODUCT_VERSION}")

        // apply JavaPlugin (required) / WarPlugin (optional) / ManifestPlugin & evaluate
        project.pluginManager.apply(WarPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main War task & emulate running task
        val task = project.tasks.getByName(WarPlugin.WAR_TASK_NAME) as War
        task.actions.forEach {
            it.execute(task)
        }

        // check not yet patched WAR archive artefact manifest file
        var content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
        Assert.assertFalse(content.contains("Resources-Project-Version: "))

        // get "patch.archives" task & emulate running task
        val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
        patchTask.actions.forEach {
            it.execute(patchTask)
        }

        // check patched WAR archive artefact manifest file
        content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
        Assert.assertTrue(
            content.contains("Resources-Project-Version: ${project.version}")
        )
    }


    /** 13) Evaluate applying the plugin with setting gradle property to patch archives explicitly to false */
    @Test fun test_Evaluate_DoNotPatch() {
        val project = ProjectBuilder.builder().build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, false)

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // assert patch task does not exit because gradle property set to "false"
        Assert.assertNull(project.tasks.findByName(ManifestPlugin.TASK_NAME))
    }


    /** 14) Evaluate applying the plugin, running WAR (but not JAR) task but patching with overwritten attribute */
    @Test fun test_Evaluate_OverwritePatchedAttributes() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir3).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}Resources-Project-Name", "")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")
        propertiesExtension.set("${ManifestPlugin.PREFIX_PATCHED}abcdef", "")

        withEnvironmentVariable(
            "${ManifestPlugin.PREFIX_PATCHED}Resources-Project-Version", "NOVERSION"
        ).and(
            "${ManifestPlugin.PREFIX_PATCHED}${ManifestPlugin.PROP_PRODUCT_VERSION}", "VERSION123"
        ).execute {
            // apply JavaPlugin (required) / WarPlugin (optional) / ManifestPlugin & evaluate
            project.pluginManager.apply(WarPlugin::class.java)
            project.pluginManager.apply(ManifestPlugin::class.java)
            project.evaluate()

            // get main War task & emulate running task
            val task = project.tasks.getByName(WarPlugin.WAR_TASK_NAME) as War
            task.actions.forEach {
                it.execute(task)
            }

            // check not yet patched WAR archive artefact manifest file
            var content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}")
                            .getManifestFileContent()
            Assert.assertFalse(content.contains("Resources-Project-Name: "))
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: "))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched WAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertTrue(content.contains("Resources-Project-Version: NOVERSION"))
            Assert.assertTrue(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: VERSION123"))
        }
    }
}


/**
 *  Simple "hack" to evaluate Project.afterEvaluate closure
 *  -> Necessary because assertions cannot be done in such a closure but attributes are not available before that!
 */
internal fun Project.evaluate() = (this as DefaultProject).evaluate()
