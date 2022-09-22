# ManifestPlugin

![example workflow](https://github.com/VISUS-Health-IT-GmbH/ManifestPlugin/actions/workflows/gradle.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_ManifestPlugin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_ManifestPlugin)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_ManifestPluginn&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_ManifestPlugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_ManifestPlugin&metric=coverage)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_ManifestPlugin)

Gradle Plugin to create general manifest attributes and extended attributes using a project extension. Also adds another
task if enabled via Gradle property to patch JAR / WAR archive artifacts and their manifest with non-cacheable
attributes.

## Usage

To find out how to apply this plugin to your Gradle project see the information over at the
[Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.visus.infrastructure.manifest)!

## Configuration

The following manifest attributes are set by default:

```manifest
Gradle-Version: ${project.gradle.gradleVersion}
Created-By: ${System.getProperty("java.runtime.version")} (${System.getProperty("java.vendor")})
Permissions: all-permissions
Codebase: *
Application-Name: ${project.name}
PROP_PRODUCT_NAME: ${project.name}
PROP_PRODUCT_VERSION: ${project.version}
PROP_PRODUCT_RC: ${project.${project.name}.rc} when found
PROP_PRODUCT_RELEASED: ${project.${project.name}.released} when found
PROP_UNIQUE_DEVICE_IDENTIFICATION_EU: ${project.${project.name}.udi_eu} when found
PROP_UNIQUE_DEVICE_IDENTIFICATION_USA: ${project.${project.name}.udi_usa} when found
PROP_VENDOR_NAME: ${project.${project.name}.vendor} when found
PROP_RELEASE_DATE: Date (only available when ${project.${project.name}.released} is true)
PROP_RELEASE_DATE_yyMMdd: Date (only available when ${project.${project.name}.released} is true)
PROP_BUILD_TIME: Time (only available when ${project.${project.name}.released} is true)
PROP_BUILD_USER: User (only available when ${project.${project.name}.released} is true)
PROP_BUILD_HOST: Host (only available when ${project.${project.name}.released} is true)

```

The manifest attributes can be overwritten or extended in the projects own gradle.properties file. Blank property values
evaluate to the manifest attribute key not being set. Overwriting or extending must be done by setting the property
equally to the manifest attribute key but prefixing it with "manifest."!

```properties
# Overwrite default property
manifest.PROP_PRODUCT_NAME=Testname

# Exclude default property
manifest.PROP_PRODUCT_RC=

# Extend manifest attributes
manifest.Main-Class=com.visus.infrastructure.MainClass
```

When the project extension name differs from the actual project name you can set the name in the project
gradle.properties file (or directly in build script before applying this plugin) using the following property:

```properties
# Used e.g. in ${project.${property.value}.rc} instead of ${project.${project.name}.rc}
plugins.manifest.properties.differentExtension=String
```

To enable the Gradle task to patch existing JAR / WAR archive artifacts, you must set the following property to true:

```properties
plugins.manifest.properties.patchArchives=Boolean
```

This will add a new Gradle task *patch.archives* which will patch the JAR / WAR archive manifest with non-cacheable
attributes such as dates or timestamps. It also tries to patch the *PROP_PRODUCT_VERSION* with the Jira ticket id,
which must be provided as system property *JIRA_TICKET* - if it is not found, it won't be patched!

There is also the possibility to add specific manifest attributes only in the patched JAR / WAR archive artifacts. Such
attributes must be set in the projects own gradle.properties file. Here another prefix must be used, "patched.manifest."
which ist stripped but only evaluated if the property enabling the Gradle *patch.archives* mentioned above is set to
true:

```properties
# Property only set in patched archive artifacts
patched.manifest.Release-Date-Time=${PROP_RELEASE_DATE} / ${PROP_BUILD_TIME}
```

### Replacement in manifest attributes

As seen in the last example, it is possible to include manifest attribute keys / property keys in another property
value. The plugin tries to substitute all property keys found, denoted with *${...}*. This is done with the normal
manifest attributes as well as the patched manifest attributes! It reads directly from the Gradle properties but can
only find properties starting with the prefix "manifest." (for non patched archive artifacts) and properties starting
with the prefix "manifest." or "patched.manifest." (for patched archive artifacts).
