/*  ManifestPluginCompanionTest.kt
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
import java.nio.file.Files

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.GradleVersion

import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 *  ManifestPluginCompanionTest:
 *  ===========================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the companion object of the ManifestPlugin class
 */
open class ManifestPluginCompanionTest {
    companion object {
        // Nice cheating: this::class.java.protectionDomain.codeSource.location -> build\classes\kotlin\test
        private val testClassLocation = File(this::class.java.protectionDomain.codeSource.location.toURI())

        // For fake root projects: They need existing file paths!
        private val buildDir = testClassLocation.parentFile.parentFile.parentFile
        private val rootProjectTestDir = File(buildDir, "test")

        // example property key used in tests
        private const val KEY_NAME  = "test123"
        private const val KEY_VALUE = "test456"
    }


    /** 0) Create temporary directory for tests */
    @Before fun configureDirectories() {
        if (rootProjectTestDir.exists() && rootProjectTestDir.isDirectory) {
            Files.walk(rootProjectTestDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map { it.toFile() }
                .forEach { it.delete() }
        }

        rootProjectTestDir.mkdirs()
    }


    /** 1) Check the "ManifestPlugin.getExtensionName" method: Property not set at all */
    @Test fun test_getExtensionName_propertyNotSet() {
        Assert.assertNull(ManifestPlugin.getExtensionName(ProjectBuilder.builder().build()))
    }


    /** 2) Check the "ManifestPlugin.getExtensionName" method: Property set in project but null */
    @Test fun test_getExtensionName_propertySetProjectButNull() {
        val p1 = ProjectBuilder.builder().build()
        p1.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, null)
        Assert.assertNull(ManifestPlugin.getExtensionName(p1))
    }


    /** 3) Check the "ManifestPlugin.getExtensionName" method: Property set in project */
    @Test fun test_getExtensionName_propertySetProject() {
        val p2 = ProjectBuilder.builder().build()
        p2.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, "p2")
        Assert.assertEquals("p2", ManifestPlugin.getExtensionName(p2))
    }


    /** 4) Check the "ManifestPlugin.getExtensionName" method: Property set in root project but null */
    @Test fun test_getExtensionName_propertySetRootProjectButNull() {
        val p3 = ProjectBuilder.builder().withProjectDir(buildDir).build()
        p3.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, null)
        Assert.assertNull(ManifestPlugin.getExtensionName(ProjectBuilder.builder().withParent(p3).build()))
    }


    /** 5) Check the "ManifestPlugin.getExtensionName" method: Property set in root project, missing in subproject */
    @Test fun test_getExtensionName_propertySetRootProject() {
        val p4 = ProjectBuilder.builder().withProjectDir(buildDir).build()
        val p4Sub = ProjectBuilder.builder().withParent(p4).build()
        p4.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, "p4")
        p4Sub.extensions.getByType(ExtraPropertiesExtension::class.java).properties.remove(ManifestPlugin.KEY_EXTENSION)
        Assert.assertEquals("p4", ManifestPlugin.getExtensionName(p4Sub))
    }


    /** 6) Check the "ManifestPlugin.getExtension" method: Property not set and project has no extension */
    @Test fun test_getExtension_propertyNotSetNoExtension() {
        Assert.assertNull(ManifestPlugin.getExtension(ProjectBuilder.builder().build()))
    }


    /** 7) Check the "ManifestPlugin.getExtension" method: Property not set but root project has extension */
    @Test fun test_getExtension_propertyNotSetRootProjectExtension() {
        val p1 = ProjectBuilder.builder().withProjectDir(buildDir).build()
        val p1Map = mapOf("test1" to "test2")
        p1.extensions.getByType(ExtraPropertiesExtension::class.java).set(p1.name, p1Map)
        Assert.assertEquals(p1Map, ManifestPlugin.getExtension(ProjectBuilder.builder().withParent(p1).build())!!)
    }


    /** 8) Check the "ManifestPlugin.getExtension" method: Property set but root project has extension */
    @Test fun test_getExtension_propertySetRootProjectExtension() {
        val p2 = ProjectBuilder.builder().withProjectDir(buildDir).build()
        val p2Key = "p2Key"
        val p2Map = mapOf("test1" to "test2")
        p2.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, p2Key)
        p2.extensions.getByType(ExtraPropertiesExtension::class.java).set(p2Key, p2Map)
        Assert.assertEquals(p2Map, ManifestPlugin.getExtension(ProjectBuilder.builder().withParent(p2).build())!!)
    }


    /** 9) Check the "ManifestPlugin.getExtension" method: Property not set but subproject has extension */
    @Test fun test_getExtension_propertyNotSetSubprojectExtension() {
        val p3 = ProjectBuilder.builder().withProjectDir(buildDir).build()
        val p3Sub = ProjectBuilder.builder().withParent(p3).build()
        val p3SubMap = mapOf("test1" to "test2")
        p3Sub.extensions.getByType(ExtraPropertiesExtension::class.java).set(p3Sub.name, p3SubMap)
        Assert.assertEquals(p3SubMap, ManifestPlugin.getExtension(p3Sub)!!)
    }


    /** 10) Check the "ManifestPlugin.getExtension" method: Property set but subproject has extension */
    @Test fun test_getExtension_propertySetSubprojectExtension() {
        val p4 = ProjectBuilder.builder().withProjectDir(buildDir).build()
        val p4Sub = ProjectBuilder.builder().withParent(p4).build()
        val p4SubKey = "p4SubKey"
        val p4SubMap = mapOf("test1" to "test2")
        p4Sub.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, p4SubKey)
        p4Sub.extensions.getByType(ExtraPropertiesExtension::class.java).set(p4SubKey, p4SubMap)
        Assert.assertEquals(p4SubMap, ManifestPlugin.getExtension(p4Sub)!!)
    }


    /** 11) Check the "ManifestPlugin.getExtension" method: Property not set but project has extension */
    @Test fun test_getExtension_propertyNotSetProjectExtension() {
        val p5 = ProjectBuilder.builder().build()
        val p5Map = mapOf("test1" to "test2")
        p5.extensions.getByType(ExtraPropertiesExtension::class.java).set(p5.name, p5Map)
        Assert.assertEquals(p5Map, ManifestPlugin.getExtension(p5)!!)
    }


    /** 12) Check the "ManifestPlugin.getExtension" method: Property set but project has extension */
    @Test fun test_getExtension_propertySetProjectExtension() {
        val p6 = ProjectBuilder.builder().build()
        val p6Key = "p6Key"
        val p6Map = mapOf("test1" to "test2")
        p6.extensions.getByType(ExtraPropertiesExtension::class.java).set(ManifestPlugin.KEY_EXTENSION, p6Key)
        p6.extensions.getByType(ExtraPropertiesExtension::class.java).set(p6Key, p6Map)
        Assert.assertEquals(p6Map, ManifestPlugin.getExtension(p6)!!)
    }


    /** 13) Check the "ManifestPlugin.getMapping" method: Mainly to check that after a change nothing is untested! */
    @Test fun test_getMapping() {
        val project = ProjectBuilder.builder().build()

        val mapping = ManifestPlugin.getMapping(project)
        Assert.assertEquals(12, mapping.size)
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.GradleVersion))
        Assert.assertEquals(project.gradle.gradleVersion, mapping[ManifestPlugin.GradleVersion])
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.CreatedBy))
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.Permissions))
        Assert.assertEquals("all-permissions", mapping[ManifestPlugin.Permissions])
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.Codebase))
        Assert.assertEquals("*", mapping[ManifestPlugin.Codebase])
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.ApplicationName))
        Assert.assertEquals(project.name, mapping[ManifestPlugin.ApplicationName])
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_PRODUCT_NAME))
        Assert.assertEquals(project.name, mapping[ManifestPlugin.PROP_PRODUCT_NAME])
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_PRODUCT_VERSION))
        Assert.assertEquals(project.version, mapping[ManifestPlugin.PROP_PRODUCT_VERSION])
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_RELEASE_DATE))
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_RELEASE_DATE_yyMMdd))
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_BUILD_USER))
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_BUILD_HOST))
        Assert.assertTrue(mapping.keys.contains(ManifestPlugin.PROP_BUILD_TIME))

    }


    /** 14) Check the "ManifestPlugin.handleSimpleEntry" method */
    @Test fun test_handleSimpleEntry() {
        val manifest = mutableMapOf<String, String>()
        val gradleProperties = mutableMapOf(KEY_NAME to KEY_VALUE) as MutableMap<*, *>

        ManifestPlugin.handleSimpleEntry(KEY_NAME, KEY_VALUE, manifest, gradleProperties)
        Assert.assertTrue(manifest.containsKey(KEY_NAME))
        Assert.assertEquals(KEY_VALUE, manifest[KEY_NAME])
        Assert.assertFalse(gradleProperties.containsKey(KEY_NAME))
    }


    /** 15) Check the "ManifestPlugin.handleEasyEntry" method: In gradle.properties but not valid */
    @Test fun test_handleEasyEntry_inPropertiesButEmpty() {
        val manifest = mutableMapOf<String, String>()
        val gradleProperties = mutableMapOf(KEY_NAME to null) as MutableMap<*, *>

        ManifestPlugin.handleEasyEntry(KEY_NAME, KEY_VALUE, manifest, gradleProperties)
        Assert.assertFalse(manifest.containsKey(KEY_NAME))
        Assert.assertFalse(gradleProperties.containsKey(KEY_NAME))
    }


    /** 16) Check the "ManifestPlugin.handleEasyEntry" method: In gradle.properties */
    @Test fun test_handleEasyEntry_inProperties() {
        val manifest = mutableMapOf<String, String>()
        val gradleProperties = mutableMapOf(KEY_NAME to KEY_VALUE) as MutableMap<*, *>

        ManifestPlugin.handleEasyEntry(KEY_NAME, KEY_VALUE, manifest, gradleProperties)
        Assert.assertTrue(manifest.containsKey(KEY_NAME))
        Assert.assertEquals(KEY_VALUE, manifest[KEY_NAME])
        Assert.assertFalse(gradleProperties.containsKey(KEY_NAME))
    }


    /** 17) Check the "ManifestPlugin.handleEasyEntry" method: Not in gradle.properties */
    @Test fun test_handleEasyEntry_notInProperties() {
        val manifest = mutableMapOf<String, String>()
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleEasyEntry(KEY_NAME, KEY_VALUE, manifest, gradleProperties)
        Assert.assertTrue(manifest.containsKey(KEY_NAME))
        Assert.assertEquals(KEY_VALUE, manifest[KEY_NAME])
        Assert.assertFalse(gradleProperties.containsKey(KEY_NAME))
    }


    /** 18) Check the "ManifestPlugin.handleDefaultEntry" method: Not in gradle.properties but project extension */
    @Test fun test_handleDefaultEntry_notInPropertiesButExtension() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf(KEY_NAME to KEY_VALUE)
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleDefaultEntry(KEY_NAME, manifest, KEY_NAME, extension, gradleProperties)
        Assert.assertTrue(manifest.containsKey(KEY_NAME))
        Assert.assertEquals(KEY_VALUE, manifest[KEY_NAME])
    }


    /** 19) Check the "ManifestPlugin.handleDefaultEntry" method: Not in gradle.properties and not project extension */
    @Test fun test_handleDefaultEntry_notInPropertiesNotExtension() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleDefaultEntry(KEY_NAME, manifest, KEY_NAME, extension, gradleProperties)
        Assert.assertFalse(manifest.containsKey(KEY_NAME))
    }


    /** 20) Check the "ManifestPlugin.handleDefaultEntry" method: In gradle.properties but project extension */
    @Test fun test_handleDefaultEntry_inPropertiesButExtension() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf(KEY_NAME to KEY_VALUE)
        val gradleProperties = mutableMapOf(KEY_NAME to KEY_VALUE) as MutableMap<*, *>

        ManifestPlugin.handleDefaultEntry(KEY_NAME, manifest, KEY_NAME, extension, gradleProperties)
        Assert.assertTrue(manifest.containsKey(KEY_NAME))
        Assert.assertEquals(KEY_VALUE, manifest[KEY_NAME])
    }


    /** 21) Check the "ManifestPlugin.handleDefaultEntry" method: In gradle.properties and not project extension */
    @Test fun test_handleDefaultEntry_inPropertiesNotExtension() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf(KEY_NAME to KEY_VALUE) as MutableMap<*, *>

        ManifestPlugin.handleDefaultEntry(KEY_NAME, manifest, KEY_NAME, extension, gradleProperties)
        Assert.assertTrue(manifest.containsKey(KEY_NAME))
        Assert.assertEquals(KEY_VALUE, manifest[KEY_NAME])
    }


    /** 22) Check the "ManifestPlugin.handleVersionEntry" method: In gradle.properties but not valid */
    @Test fun test_handleVersionEntry_inPropertiesButEmpty() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf(ManifestPlugin.VERSION to "") as MutableMap<*, *>

        ManifestPlugin.handleVersionEntry(ManifestPlugin.VERSION, GradleVersion.current().version, manifest, extension,
                                          gradleProperties)
        Assert.assertFalse(manifest.containsKey(ManifestPlugin.VERSION))
        Assert.assertFalse(gradleProperties.containsKey(ManifestPlugin.VERSION))
    }


    /** 23) Check the "ManifestPlugin.handleVersionEntry" method: In gradle.properties */
    @Test fun test_handleVersionEntry_inProperties() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf(ManifestPlugin.VERSION to "1.2.3.4") as MutableMap<*, *>

        ManifestPlugin.handleVersionEntry(ManifestPlugin.VERSION, GradleVersion.current().version, manifest, extension,
                                          gradleProperties)
        Assert.assertTrue(manifest.containsKey(ManifestPlugin.VERSION))
        Assert.assertEquals("1.2.3.4", manifest[ManifestPlugin.VERSION])
        Assert.assertFalse(gradleProperties.containsKey(ManifestPlugin.VERSION))
    }


    /** 24) Check the "ManifestPlugin.handleEasyEntry" method: Not in gradle.properties / extension -> unspecified */
    @Test fun test_handleEasyEntry_notInPropertiesNotInExtensionAndUnspecified() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleVersionEntry(ManifestPlugin.VERSION, "unspecified", manifest, extension,
                                          gradleProperties)
        Assert.assertFalse(manifest.containsKey(ManifestPlugin.VERSION))
        Assert.assertFalse(gradleProperties.containsKey(ManifestPlugin.VERSION))
    }


    /** 25) Check the "ManifestPlugin.handleEasyEntry" method: Not in gradle.properties / extension -> blank */
    @Test fun test_handleEasyEntry_notInPropertiesNotInExtensionAndBlank() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleVersionEntry(ManifestPlugin.VERSION, "", manifest, extension,
                                          gradleProperties)
        Assert.assertFalse(manifest.containsKey(ManifestPlugin.VERSION))
        Assert.assertFalse(gradleProperties.containsKey(ManifestPlugin.VERSION))
    }


    /** 26) Check the "ManifestPlugin.handleEasyEntry" method: Not in gradle.properties / extension -> correct */
    @Test fun test_handleEasyEntry_notInPropertiesNotInExtensionAndCorrect() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf<String, String>()
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleVersionEntry(ManifestPlugin.VERSION, GradleVersion.current().version, manifest, extension,
                                          gradleProperties)
        Assert.assertTrue(manifest.containsKey(ManifestPlugin.VERSION))
        Assert.assertEquals(GradleVersion.current().version, manifest[ManifestPlugin.VERSION])
        Assert.assertFalse(gradleProperties.containsKey(ManifestPlugin.VERSION))
    }


    /** 27) Check the "ManifestPlugin.handleEasyEntry" method: Not in gradle.properties but in extension */
    @Test fun test_handleEasyEntry_notInPropertiesInExtension() {
        val manifest = mutableMapOf<String, String>()
        val extension = mapOf(ManifestPlugin.VERSION to "1.2.3.4")
        val gradleProperties = mutableMapOf<String, String>() as MutableMap<*, *>

        ManifestPlugin.handleVersionEntry(ManifestPlugin.VERSION, GradleVersion.current().version, manifest, extension,
            gradleProperties)
        Assert.assertTrue(manifest.containsKey(ManifestPlugin.VERSION))
        Assert.assertEquals("1.2.3.4", manifest[ManifestPlugin.VERSION])
        Assert.assertFalse(gradleProperties.containsKey(ManifestPlugin.VERSION))
    }
}
