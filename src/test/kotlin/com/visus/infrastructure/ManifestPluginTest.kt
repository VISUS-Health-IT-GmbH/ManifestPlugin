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

import java.net.InetAddress
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.GradleVersion

import org.junit.Assert
import org.junit.Test

import com.visus.infrastructure.exception.JavaPluginMissingException


/**
 *  ManifestPluginTest:
 *  ==================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the ManifestPlugin class
 */
open class ManifestPluginTest {
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

        // get main Jar task
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

        // get main Jar task
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
        propertiesExtension.set("${ManifestPlugin.prefix}PROP_USER_NAME", "hahnen")
        propertiesExtension.set("${ManifestPlugin.prefix}PROP_USER_BIRTHDATE", "11.07.1998")
        propertiesExtension.set("${ManifestPlugin.prefix}PROP_USER_INFO", "\${PROP_USER_NAME}/\${PROP_USER_BIRTHDATE}")
        propertiesExtension.set("${ManifestPlugin.prefix}${ManifestPlugin.PROP_PRODUCT_RELEASED}", false)

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task
        val attributes = (project.tasks.getByName(JavaPlugin.JAR_TASK_NAME) as Jar).manifest.attributes
        Assert.assertTrue(attributes.containsKey("PROP_USER_NAME"))
        Assert.assertEquals("hahnen", attributes["PROP_USER_NAME"])
        Assert.assertTrue(attributes.containsKey("PROP_USER_BIRTHDATE"))
        Assert.assertEquals("11.07.1998", attributes["PROP_USER_BIRTHDATE"])
        Assert.assertTrue(attributes.containsKey("PROP_USER_INFO"))
        Assert.assertEquals("hahnen/11.07.1998", attributes["PROP_USER_INFO"])
        Assert.assertTrue(attributes.containsKey(ManifestPlugin.PROP_PRODUCT_RELEASED))
        Assert.assertEquals("false", attributes[ManifestPlugin.PROP_PRODUCT_RELEASED])
    }


    /** 7) Evaluate the properties only set when project released */
    @Test fun test_Evaluate_releasedProperties() {
        val project = ProjectBuilder.builder().build()

        // emulate gradle.properties
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set("${ManifestPlugin.prefix}${ManifestPlugin.PROP_PRODUCT_RELEASED}", true)

        // apply JavaPlugin (required) / ManifestPlugin & evaluate
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(ManifestPlugin::class.java)
        project.evaluate()

        // get main Jar task
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


    // TODO: Add more evaluation tests using special configurations and real project extensions!
}


/**
 *  Simple "hack" to evaluate Project.afterEvaluate closure
 *  -> Necessary because assertions cannot be done in such a closure but attributes are not available before that!
 */
internal fun Project.evaluate() = (this as DefaultProject).evaluate()
