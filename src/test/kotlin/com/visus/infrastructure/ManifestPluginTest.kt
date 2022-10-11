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

        private val projectProjectDir4  = File(buildDir, "${ManifestPluginTest::class.simpleName}_4")
        private val projectBuildDir4    = File(projectProjectDir4, "build")
        private val projectLibsDir4     = File(projectBuildDir4, "libs")

        private val projectProjectDir5  = File(buildDir, "${ManifestPluginTest::class.simpleName}_5")
        private val projectBuildDir5    = File(projectProjectDir5, "build")
        private val projectLibsDir5     = File(projectBuildDir5, "libs")

        private val projectProjectDir6  = File(buildDir, "${ManifestPluginTest::class.simpleName}_6")
        private val projectBuildDir6    = File(projectProjectDir6, "build")
        private val projectLibsDir6     = File(projectBuildDir6, "libs")

        private val projectProjectDir7  = File(buildDir, "${ManifestPluginTest::class.simpleName}_7")
        private val projectBuildDir7    = File(projectProjectDir7, "build")
        private val projectLibsDir7     = File(projectBuildDir7, "libs")

        private val projectProjectDir8  = File(buildDir, "${ManifestPluginTest::class.simpleName}_8")
        private val projectBuildDir8    = File(projectProjectDir8, "build")
        private val projectLibsDir8     = File(projectBuildDir8, "libs")

        private val projectProjectDir9  = File(buildDir, "${ManifestPluginTest::class.simpleName}_9")
        private val projectBuildDir9    = File(projectProjectDir9, "build")
        private val projectLibsDir9     = File(projectBuildDir9, "libs")

        private val projectProjectDir10  = File(buildDir, "${ManifestPluginTest::class.simpleName}_10")
        private val projectBuildDir10    = File(projectProjectDir10, "build")
        private val projectLibsDir10     = File(projectBuildDir10, "libs")

        private val projectProjectDir11  = File(buildDir, "${ManifestPluginTest::class.simpleName}_11")
        private val projectBuildDir11    = File(projectProjectDir11, "build")
        private val projectLibsDir11     = File(projectBuildDir11, "libs")

        private val projectProjectDir12  = File(buildDir, "${ManifestPluginTest::class.simpleName}_12")
        private val projectBuildDir12    = File(projectProjectDir12, "build")
        private val projectLibsDir12     = File(projectBuildDir12, "libs")

        private val projectProjectDir13  = File(buildDir, "${ManifestPluginTest::class.simpleName}_13")
        private val projectBuildDir13    = File(projectProjectDir13, "build")
        private val projectLibsDir13     = File(projectBuildDir13, "libs")


        /** 0) Create temporary directories for tests */
        @BeforeClass @JvmStatic fun configureTestsuite() {
            // i) remove directories if exists
            listOf(
                projectProjectDir1, projectProjectDir2, projectProjectDir3, projectProjectDir4, projectProjectDir5,
                projectProjectDir6, projectProjectDir7, projectProjectDir8, projectProjectDir9, projectProjectDir10,
                projectProjectDir11, projectProjectDir12, projectProjectDir13
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
                projectLibsDir1, projectLibsDir2, projectLibsDir3, projectLibsDir4, projectLibsDir5, projectLibsDir6,
                projectLibsDir7, projectLibsDir8, projectLibsDir9, projectLibsDir10, projectLibsDir11, projectLibsDir12,
                projectLibsDir13
            ).forEach { it.mkdirs() }
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
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_BUILD_DATE))
        Assert.assertEquals(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            attributes[ManifestPlugin.PROP_BUILD_DATE])
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
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir1).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)
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
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir2).build()

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


    /**
     *  15) VISUS-12: Test that ${PROP_PRODUCT_RC} will be replaced even though deactivated in manifest attributes
     *                -> manifest.PROP_PRODUCT_RC=
     *                -> manifest.TEST-ATTRIBUTE1=${PROP_PRODUCT_RC}
     *                -> patched.manifest.TEST_ATTRIBUTE2=${PROP_PRODUCT_RC}
     */
    @Test fun test_VISUS12_BetterMapping1() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir4).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(
            project.name, mapOf(
                "rc" to "RC01"
            )
        )
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_RC}", "")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}TEST-ATTRIBUTE1", "\${PROP_PRODUCT_RC}")
        propertiesExtension.set("${ManifestPlugin.PREFIX_PATCHED}TEST-ATTRIBUTE2", "\${PROP_PRODUCT_RC}")

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task & emulate running task
        val task = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar
        task.actions.forEach {
            it.execute(task)
        }

        // check not yet patched WAR archive artefact manifest file
        var content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}")
            .getManifestFileContent()
        Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_RC}: "))
        Assert.assertTrue(content.contains("TEST-ATTRIBUTE1: RC01"))

        // get "patch.archives" task & emulate running task
        val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
        patchTask.actions.forEach {
            it.execute(patchTask)
        }

        // check patched WAR archive artefact manifest file
        content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
        Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_RC}: "))
        Assert.assertTrue(content.contains("TEST-ATTRIBUTE2: RC01"))
    }


    /**
     *  16) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will be replaced even though deactivated in manifest attributes
     *                -> manifest.PROP_PRODUCT_VERSION=
     *                -> manifest.TEST-ATTRIBUTE1=${PROP_PRODUCT_VERSION}
     *                -> patched.manifest.TEST_ATTRIBUTE2=${PROP_PRODUCT_VERSION}
     */
    @Test fun test_VISUS12_BetterMapping2() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir5).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(
            project.name, mapOf(
                "version" to "1.2.3.4"
            )
        )
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}TEST-ATTRIBUTE1", "\${PROP_PRODUCT_VERSION}")
        propertiesExtension.set("${ManifestPlugin.PREFIX_PATCHED}TEST-ATTRIBUTE2", "\${PROP_PRODUCT_VERSION}")

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task & emulate running task
        val task = project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar
        task.actions.forEach {
            it.execute(task)
        }

        // check not yet patched WAR archive artefact manifest file
        var content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}")
            .getManifestFileContent()
        Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: "))
        Assert.assertTrue(content.contains("TEST-ATTRIBUTE1: 1.2.3.4"))

        // get "patch.archives" task & emulate running task
        val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
        patchTask.actions.forEach {
            it.execute(patchTask)
        }

        // check patched WAR archive artefact manifest file
        content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
        Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: "))
        Assert.assertTrue(content.contains("TEST-ATTRIBUTE2: 1.2.3.4"))
    }


    /**
     *  17) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will be in patched manifest attributes with ticket id even
     *      though it is disabled in normal manifest attributes and strictly enabled by custom property
     *      -> version available, manually set
     */
    @Test fun test_VISUS12_VersionCorrectlyPatched() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir6).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")

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
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: "))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertTrue(
                content.contains(
                    "${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}.${System.getProperty(ManifestPlugin.SYS_TICKET)}"
                )
            )
        }
    }


    /**
     *  18) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will be in patched manifest attributes with ticket id even
     *      though it was not disabled in normal manifest attributes and strictly enabled by custom property
     *      -> version available, manually set
     */
    @Test fun test_VISUS12_VersionWronglyPatched() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir7).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)

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
            Assert.assertTrue(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertTrue(
                content.contains(
                    "${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}.${System.getProperty(ManifestPlugin.SYS_TICKET)}"
                )
            )
        }
    }


    /** 19) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will not be patched if property strictly disabled */
    @Test fun test_VISUS12_VersionShouldNotBePatched() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir8).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, false)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")

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
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))
        }
    }


    /** 20) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will not be patched as no version available */
    @Test fun test_VISUS12_VersionNotAvailable() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir9).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)

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
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))
        }
    }


    /**
     *  21) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will be in patched manifest attributes with build id even
     *      though it is disabled in normal manifest attributes and strictly enabled by custom property
     *      -> version available, manually set
     */
    @Test fun test_VISUS12_VersionCorrectlyPatchedBuildId() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir10).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")

        restoreSystemProperties {
            System.setProperty(ManifestPlugin.SYS_TICKET, "VISUS-1234")
            System.setProperty(ManifestPlugin.SYS_BUILD, "1337")

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
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: "))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertTrue(
                content.contains(
                    "${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}." +
                    "${System.getProperty(ManifestPlugin.SYS_TICKET)}-" +
                    System.getProperty(ManifestPlugin.SYS_BUILD)
                )
            )
        }
    }


    /**
     *  22) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will be in patched manifest attributes with build id even
     *      though it was not disabled in normal manifest attributes and strictly enabled by custom property
     *      -> version available, manually set
     */
    @Test fun test_VISUS12_VersionWronglyPatchedBuildId() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir11).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)

        restoreSystemProperties {
            System.setProperty(ManifestPlugin.SYS_TICKET, "VISUS-1234")
            System.setProperty(ManifestPlugin.SYS_BUILD, "1337")

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
            Assert.assertTrue(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertTrue(
                content.contains(
                    "${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}." +
                    "${System.getProperty(ManifestPlugin.SYS_TICKET)}-" +
                    System.getProperty(ManifestPlugin.SYS_BUILD)
                )
            )
        }
    }


    /** 23) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will not be patched if property strictly disabled (build id) */
    @Test fun test_VISUS12_VersionShouldNotBePatchedBuildId() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir12).build().also {
            it.version = GradleVersion.current().version
        }

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, false)
        propertiesExtension.set("${ManifestPlugin.PREFIX_DEFAULT}${ManifestPlugin.PROP_PRODUCT_VERSION}", "")

        restoreSystemProperties {
            System.setProperty(ManifestPlugin.SYS_TICKET, "VISUS-1234")
            System.setProperty(ManifestPlugin.SYS_BUILD, "1337")

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
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))
        }
    }


    /** 24) VISUS-12: Test that ${PROP_PRODUCT_VERSION} will not be patched as no version available (build id) */
    @Test fun test_VISUS12_VersionNotAvailableBuildId() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir13).build()

        // emulate gradle.properties (should patch and some specific properties only available in patched archive)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(ManifestPlugin.KEY_PATCH, true)
        propertiesExtension.set(ManifestPlugin.KEY_VERSION, true)

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
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))

            // get "patch.archives" task & emulate running task
            val patchTask = project.tasks.findByName(ManifestPlugin.TASK_NAME) as Task
            patchTask.actions.forEach {
                it.execute(patchTask)
            }

            // check patched JAR archive artefact manifest file
            content = project.file("${project.buildDir}/libs/${task.archiveFileName.get()}").getManifestFileContent()
            Assert.assertFalse(content.contains("${ManifestPlugin.PROP_PRODUCT_VERSION}: ${project.version}"))
        }
    }
}


/**
 *  Simple "hack" to evaluate Project.afterEvaluate closure
 *  -> Necessary because assertions cannot be done in such a closure but attributes are not available before that!
 */
internal fun Project.evaluate() = (this as DefaultProject).evaluate()
