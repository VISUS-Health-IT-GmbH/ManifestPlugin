/*  ManifestPlugin.kt
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.kotlin.dsl.extra

import com.visus.infrastructure.exception.JavaPluginMissingException
import com.visus.infrastructure.extension.checkValuePossiblyExists
import com.visus.infrastructure.extension.checkValueTrulyExists
import com.visus.infrastructure.extension.patchManifest
import com.visus.infrastructure.extension.substituteProperties


/**
 *  ManifestPlugin:
 *  ==============
 *
 *  @author Tobias Hahnen
 *
 *  Plugin to create specific manifest attributes. Allows use to add / overwrite custom attributes.
 *
 *  TODO: Add property to disable warnings.
 *  TODO: Allow overwriting properties using environment variables / system properties.
 *  TODO: Add "patch.jar" to patch every JAR archive with specific properties.
 *  TODO: Add "patch.war" to patch every WAR archive with specific properties.
 */
open class ManifestPlugin : Plugin<Project> {
    companion object {
        // identifiers of the properties needed by this plugin
        internal const val KEY_EXTENSION                            = "plugins.manifest.properties.differentExtension"
        internal const val KEY_PATCH                                = "plugins.manifest.properties.patchArchives"

        // prefix for attributes
        internal const val PREFIX_DEFAULT                           = "manifest."
        internal const val PREFIX_PATCHED                           = "patched.manifest."

        // identifiers of default properties
        internal const val GradleVersion                            = "Gradle-Version"
        internal const val CreatedBy                                = "Created-By"
        internal const val Permissions                              = "Permissions"
        internal const val Codebase                                 = "Codebase"
        internal const val ApplicationName                          = "Application-Name"
        internal const val PROP_PRODUCT_NAME                        = "PROP_PRODUCT_NAME"
        internal const val PROP_PRODUCT_VERSION                     = "PROP_PRODUCT_VERSION"
        internal const val PROP_PRODUCT_RC                          = "PROP_PRODUCT_RC"
        internal const val PROP_PRODUCT_RELEASED                    = "PROP_PRODUCT_RELEASED"
        internal const val PROP_UNIQUE_DEVICE_IDENTIFICATION_USA    = "PROP_UNIQUE_DEVICE_IDENTIFICATION_USA"
        internal const val PROP_UNIQUE_DEVICE_IDENTIFICATION_EU     = "PROP_UNIQUE_DEVICE_IDENTIFICATION_EU"
        internal const val PROP_VENDOR_NAME                         = "PROP_VENDOR_NAME"
        internal const val PROP_RELEASE_DATE                        = "PROP_RELEASE_DATE"
        internal const val PROP_RELEASE_DATE_yyMMdd                 = "PROP_RELEASE_DATE_yyMMdd"
        internal const val PROP_BUILD_TIME                          = "PROP_BUILD_TIME"
        internal const val PROP_BUILD_USER                          = "PROP_BUILD_USER"
        internal const val PROP_BUILD_HOST                          = "PROP_BUILD_HOST"
        internal const val MainClass                                = "Main-Class"

        // identifiers of extension properties
        internal const val VERSION                                  = "version"
        internal const val RC                                       = "rc"
        internal const val RELEASED                                 = "released"
        internal const val UDI_EU                                   = "udi_eu"
        internal const val UDI_USA                                  = "udi_usa"
        internal const val VENDOR                                   = "vendor"


        /**
         *  Try to get the (root) project extension name based on the property key
         *
         *  @param target the Gradle project to search for extension
         *  @return the extension name if found, null otherwise
         *
         *  -> default: project.ext["${project.name}"] as Map<*, *>
         *  -> custom:  project.ext["${project.ext[KEY_EXTENSION]}"] as Map<*, *>
         *  -> Subproject always overrides root project! Is it true though?!
         */
        internal fun getExtensionName(target: Project) : String? = when {
            target.properties.containsKey(KEY_EXTENSION)                -> target.properties[KEY_EXTENSION]
            target.rootProject.properties.containsKey(KEY_EXTENSION)    -> target.rootProject.properties[KEY_EXTENSION]
            else                                                        -> null
        } as String?


        /**
         *  Try to get the (root) project extension based on the extension name found in project
         *
         *  @param target the Gradle project to search for extension
         *  @return the extension if found, null otherwise
         */
        internal fun getExtension(target: Project) : Map<*, *>? = with(getExtensionName(target)) {
            when {
                // i) single project or multi project root project (different extension name)
                target.rootProject == target && this != null && target.extra.has(this)
                    -> target.extra[this]

                // ii) single project or multi project root project
                target.rootProject == target && target.extra.has(target.name)
                    -> target.extra[target.name]

                // iii) multi project sub project (different extension name)
                target.rootProject != target && this != null && target.extra.has(this)
                    -> target.extra[this]

                // iv) multi project sub project
                target.rootProject != target && target.extra.has(target.name)
                    -> target.extra[target.name]

                // v) multi project sub project but root project (different extension name)
                target.rootProject != target && this != null && target.rootProject.extra.has(this)
                    -> target.rootProject.extra[this]

                // vi) multi project sub project but root project
                target.rootProject != target && target.rootProject.extra.has(target.rootProject.name)
                    -> target.rootProject.extra[target.rootProject.name]

                // vii) extension not available
                else -> run {
                    // - Warn that no extension was found
                    target.logger.warn(
                        "[ManifestPlugin.getExtension : WARNING] No available (root) project extension was found. " +
                        "Check for the property '$KEY_EXTENSION' in (root) project extension if set, otherwise there " +
                        "must have been a mistake and no (root) project extension was found by the (root) project " +
                        "name. Maybe it was deleted before this 'apply plugin' statement?"
                    )

                    // - Warn that some properties might not be available at all or will be set from Gradle project
                    //   information (PROP_PRODUCT_VERSION -> project.version)
                    target.logger.warn(
                        "[ManifestPlugin.getExtension : WARNING] The following (optional) extension properties won't " +
                        "be available: 'rc' ($PROP_PRODUCT_RC), 'released' ($PROP_PRODUCT_RELEASED), 'udi_eu' " +
                        "($PROP_UNIQUE_DEVICE_IDENTIFICATION_EU), 'udi_usa' " +
                        "($PROP_UNIQUE_DEVICE_IDENTIFICATION_USA), 'vendor' ($PROP_PRODUCT_VERSION). The (optional) " +
                        "extension property 'version' ($PROP_PRODUCT_VERSION) will be replaced with " +
                        "'${target.version}' (project.version)!"
                    )

                    null
                }
            } as Map<*, *>?
        }


        /**
         *  Get mapping of some specific manifest attributes which can be used even though the attributes themselves
         *  are not provided to manifest file. Black magic fuckery, I know ;)
         *
         *  @param target the Gradle project to search for extension
         *  @return the mapping (cannot be static) but must be created at runtime
         */
        internal fun getMapping(target: Project) : Map<String, Any> = mapOf(
            GradleVersion to target.gradle.gradleVersion,
            CreatedBy to "${System.getProperty("java.runtime.version")} (${System.getProperty("java.vendor")})",
            Permissions to "all-permissions",
            Codebase to "*",
            ApplicationName to target.name,
            PROP_PRODUCT_NAME to target.name,
            PROP_PRODUCT_VERSION to target.version,
            PROP_RELEASE_DATE to LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            PROP_RELEASE_DATE_yyMMdd to LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")),
            PROP_BUILD_USER to System.getProperty("user.name"),
            PROP_BUILD_HOST to InetAddress.getLocalHost().hostName,
            PROP_BUILD_TIME to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        )


        /**
         *  Working with the manifest this method is used to abstract the handling of simple entries
         *
         *  @param manifestKey how it is set in the META-INF/MANIFEST.MF file
         *  @param manifestValue what the key is set to
         *  @param manifest abstraction of the META-INF/MANIFEST.MF file
         *  @param gradleProperties project properties
         */
        internal fun handleSimpleEntry(manifestKey: String, manifestValue: String, manifest: MutableMap<String, String>,
                                       gradleProperties: MutableMap<*, *>) = with (manifestKey) {
            manifest[this] = manifestValue
            gradleProperties.remove(this)
        }


        /**
         *  Working with the manifest this method is used to abstract the handling of easy entries
         *
         *  @param manifestKey how it is set in the META-INF/MANIFEST.MF file
         *  @param manifestValue what the key is set to
         *  @param manifest abstraction of the META-INF/MANIFEST.MF file
         *  @param gradleProperties project properties
         */
        internal fun handleEasyEntry(manifestKey: String, manifestValue: String, manifest: MutableMap<String, String>,
                                     gradleProperties: MutableMap<*, *>) = with(manifestKey) {
            when {
                // not in gradle.properties: use project.name
                !gradleProperties.containsKey(this) -> manifest[this] = manifestValue

                // in gradle.properties and not empty: use this one
                gradleProperties.checkValueTrulyExists(this) -> manifest[this] = gradleProperties[this].toString()

                // in gradle.properties but empty: do not set the property
            }

            gradleProperties.remove(this)
        }


        /**
         *  Working with the manifest entries this method is used to abstract the handling of such default properties
         *
         *  @param manifestKey how it is set in the META-INF/MANIFEST.MF file
         *  @param manifest abstraction of the META-INF/MANIFEST.MF file
         *  @param extensionKey how it is set in the project extension
         *  @param extension abstraction of the project extension
         *  @param gradleProperties project properties
         *
         *  TODO: manifestKey / extensionKey is not the same yet!
         */
        internal fun handleDefaultEntry(manifestKey: String, manifest: MutableMap<String, String>, extensionKey: String,
                                        extension: Map<*, *>?, gradleProperties: MutableMap<*, *>) = with(manifestKey) {
            val propertyAvailable = extension.checkValuePossiblyExists(extensionKey)

            when {
                // not in gradle.properties / propertyAvailable = true: use extension[${propertyKeyExtension}]
                !gradleProperties.containsKey(this) && propertyAvailable
                    -> manifest[this] = extension!![extensionKey].toString()

                // in gradle.properties and not empty (propertyAvailable does not matter): use this one
                gradleProperties.checkValueTrulyExists(this)
                    -> manifest[this] = gradleProperties[this].toString()

                // not in gradle.properties / propertyAvailable = false: cannot set the property
                // in gradle.properties but empty: do not set the property
            }

            gradleProperties.remove(this)
        }


        /**
         *  Working with the manifest entries this method is used to abstract the handling of such default properties
         *
         *  @param manifestKey how it is set in the META-INF/MANIFEST.MF file
         *  @param version value from project.version
         *  @param manifest abstraction of the META-INF/MANIFEST.MF file
         *  @param extension abstraction of the project extension
         *  @param gradleProperties project properties
         */
        internal fun handleVersionEntry(manifestKey: String, version: Any, manifest: MutableMap<String, String>,
                                        extension: Map<*, *>?, gradleProperties: MutableMap<*, *>) = with(manifestKey) {
            val versionAvailable = extension.checkValuePossiblyExists(VERSION)

            when {
                // not in gradle.properties / versionAvailable = true: use extension[${VERSION}]
                !gradleProperties.containsKey(this) && versionAvailable
                    -> manifest[this] = extension!![VERSION].toString()

                // not in gradle.properties / versionAvailable = false: use project.version (only when correct)
                !gradleProperties.containsKey(this) && version.toString().isNotBlank()
                                                    && !version.toString().contains("unspecified")
                    -> manifest[this] = version as String

                // in gradle.properties and not empty (versionAvailable does not matter): use this one
                gradleProperties.checkValueTrulyExists(this)
                    -> manifest[this] = gradleProperties[this].toString()

                // in gradle.properties but empty: do not set the property
            }

            gradleProperties.remove(this)
        }
    }


    /** Overrides the abstract "apply" function */
    override fun apply(target: Project) {
        // 1) Check if Java plugin was applied (transitively applied when WAR plugin applied)
        if (!target.plugins.hasPlugin(JavaPlugin::class.java)) {
            throw JavaPluginMissingException(
                "[${this::class.simpleName}] Applying this plugin is not necessary as the Java plugin was neither " +
                "applied directly or transitively to the current project! Maybe the plugin is applied afterwards?"
            )
        }
        val warPluginFound = target.plugins.hasPlugin(WarPlugin::class.java)

        // 2) Get (root) project extension if available & mapping
        //    TODO: Move to "afterEvaluate" block, maybe tests must be fixed
        val extension = getExtension(target)
        val mappings = getMapping(target)

        target.afterEvaluate {
            // all properties starting with "manifest."
            val manifestGradleProperties = target.properties.filter { it.key.startsWith(PREFIX_DEFAULT) }
                .mapKeys { it.key.replace(PREFIX_DEFAULT, "") }
                .toMutableMap()
            val manifest: MutableMap<String, String> = mutableMapOf()

            // 3) Get all tasks based on WAR plugin applied -> there is at least one JAR task after applying Java plugin
            val tasks = when (warPluginFound) {
                true -> listOf(target.tasks.withType(Jar::class.java), target.tasks.withType(War::class.java))
                else -> listOf(target.tasks.withType(Jar::class.java))
            }

            // 4) Properties not based on "PROP_PRODUCT_RELEASED"
            listOf(GradleVersion, CreatedBy, Permissions, Codebase, ApplicationName, PROP_PRODUCT_NAME).forEach {
                handleEasyEntry(it, mappings[it]!! as String, manifest, manifestGradleProperties)
            }
            handleVersionEntry(
                PROP_PRODUCT_VERSION, mappings[PROP_PRODUCT_VERSION]!!, manifest, extension, manifestGradleProperties
            )
            handleDefaultEntry(PROP_PRODUCT_RC, manifest, RC, extension, manifestGradleProperties)
            handleDefaultEntry(PROP_PRODUCT_RELEASED, manifest, RELEASED, extension, manifestGradleProperties)
            handleDefaultEntry(
                PROP_UNIQUE_DEVICE_IDENTIFICATION_EU, manifest, UDI_EU, extension, manifestGradleProperties
            )
            handleDefaultEntry(
                PROP_UNIQUE_DEVICE_IDENTIFICATION_USA, manifest, UDI_USA, extension, manifestGradleProperties
            )
            handleDefaultEntry(PROP_VENDOR_NAME, manifest, VENDOR, extension, manifestGradleProperties)

            // 5) Properties based on "PROP_PRODUCT_RELEASED"
            if (manifest.containsKey(PROP_PRODUCT_RELEASED)
                && manifest[PROP_PRODUCT_RELEASED].equals("true", ignoreCase = true)) {
                listOf(
                    PROP_RELEASE_DATE, PROP_RELEASE_DATE_yyMMdd, PROP_BUILD_USER, PROP_BUILD_HOST, PROP_BUILD_TIME
                ).forEach {
                    handleSimpleEntry(it, mappings[it]!! as String, manifest, manifestGradleProperties)
                }
            }

            // 6) Other properties (starting with "manifest." but not already removed) + property substitution
            manifestGradleProperties.forEach { manifest[it.key] = it.value.toString() }
            manifest.substituteProperties(mappings)

            // 7) Add properties to tasks
            tasks.forEach {
                it.forEach { task ->
                    task.manifest.attributes.putAll(manifest)

                    // Do not add "Main-Class" to any WAR archive!
                    when (task) {
                        is War -> task.manifest.attributes.remove(MainClass)
                    }
                }
            }

            // 8) Add "patch.jar" / "patch.war" tasks to patch all archives
            val patchedManifestGradleProperties = target.properties.filter { it.key.startsWith(PREFIX_PATCHED) }
                .mapKeys { it.key.replace(PREFIX_PATCHED, "") }
                .toMutableMap()
            val patchedManifest: MutableMap<String, String> = mutableMapOf()

            listOf(PROP_BUILD_USER, PROP_BUILD_HOST, PROP_BUILD_TIME).forEach {
                handleSimpleEntry(it, mappings[it]!! as String, patchedManifest, patchedManifestGradleProperties)
            }
            patchedManifestGradleProperties.forEach { patchedManifest[it.key] = it.value.toString() }
            patchedManifest.substituteProperties(mappings, manifest)

            target.tasks.register("patch.archives") {
                doLast {
                    tasks.forEach {
                        it.forEach { task ->
                            task.patchManifest(patchedManifest)
                        }
                    }
                }
            }
        }
    }
}
