/*  MutableMapExtension.kt
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


/**
 *  Extension to MutableMap<String, String> to substitute properties notated with "${...}" in property values
 *
 *  @param mapping mapping of specific keys to values, e.g. "PROP_PRODUCT_NAME -> project.Name"
 *  @param otherMap another map to check for properties to be substituted
 */
internal fun MutableMap<String, String>.substituteProperties(mapping: Map<String, Any>,
                                                             otherMap: Map<String, String>? = null) {
    this.forEach { (key, value) ->
        var nValue = value

        "(?!:.*)(?=(\\\$\\{[^}{\$]*}))(?!:.+)".toRegex().findAll(nValue).map { it.groupValues[1] }.forEach {
            when {
                // i) check this mutable map
                this.keys.contains(it.replace("\${", "").replace("}", ""))
                    -> nValue = nValue.replace(it, this[it.replace("\${", "").replace("}", "")]!!)

                // ii) check other mutable map if not null
                otherMap != null && otherMap.keys.contains(it.replace("\${", "").replace("}", ""))
                    -> nValue = nValue.replace(it, otherMap[it.replace("\${", "").replace("}", "")]!!)

                // iii) check mapping provided
                mapping.keys.contains(it.replace("\${", "").replace("}", ""))
                    -> nValue = nValue.replace(it, mapping[it.replace("\${", "").replace("}", "")]!! as String)
            }
        }

        this[key] = nValue
    }
}


/**
 *  Extension to MutableMap<String, String> to overwrite map entries with environment variables or system properties if
 *  found, otherwise does nothing. System properties / environment variables will be added later if not already used.
 *
 *  @param prefix possible prefix in environment variables or system properties, defaults to no prefix
 */
internal fun MutableMap<String, String>.overwriteEnvironmentVariablesSystemProperty(prefix: String = "") {
    // i) get all environment variables starting with this prefix
    val prefixEnvironmentVariables = System.getenv().filter { it.key.startsWith(prefix) }
                                        .mapKeys { it.key.replace(prefix, "") }
                                        .toMutableMap()

    // ii) get all system properties starting with this prefix
    val prefixSystemProperties = System.getProperties().filter { (it.key as String).startsWith(prefix) }
                                    .mapKeys { (it.key as String).replace(prefix, "") }
                                    .toMutableMap()

    // iii) replace all keys in map with environment variable / system property if found
    this.forEach { (key, _) ->
        if (prefixEnvironmentVariables.containsKey(key)) {
            this[key] = prefixEnvironmentVariables[key] as String
            prefixEnvironmentVariables.remove(key)
            prefixSystemProperties.remove(key)
        } else if (prefixSystemProperties.containsKey(key)) {
            this[key] = prefixSystemProperties[key] as String
            prefixSystemProperties.remove(key)
        }
    }

    // iv) remove all system properties already set as environment variables and add everything to map
    prefixEnvironmentVariables.forEach { (key, value) ->
        this[key] = value
        prefixSystemProperties.remove(key)
    }
    prefixSystemProperties.forEach { (key, value) -> this[key] = value as String }
}
