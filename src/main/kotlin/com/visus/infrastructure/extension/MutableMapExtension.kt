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
