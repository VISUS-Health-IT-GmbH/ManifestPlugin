/*  MutableMapExtensionTest.kt
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

import org.junit.Assert
import org.junit.Test

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties
import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

import com.visus.infrastructure.ManifestPlugin


/**
 *  MutableMapExtensionTest:
 *  =======================
 *
 *  @author Tobias Hahnen
 *
 *  jUnit test cases on the MutableMap extension methods defined
 */
open class MutableMapExtensionTest {
    /** 1) Check the "MutableMap<String, String>.substituteProperties" method: no other map provided */
    @Test fun test_substituteProperties_noOtherMap() {
        val extension = mutableMapOf(
            "PROP_TEST1" to "TEST123",
            "PROP_TEST2" to "\${PROP_TEST1}",
            "PROP_TEST3" to "\${PROP_MAPPING_1}",
            "PROP_TEST4" to "\${PROP_NOT_MAPPED}"
        )

        // run substitution on extension (MutableMap<String, String>)
        extension.substituteProperties(mapOf("PROP_MAPPING_1" to "inMapping"))

        Assert.assertEquals("TEST123", extension["PROP_TEST1"])
        Assert.assertEquals("TEST123", extension["PROP_TEST2"])
        Assert.assertEquals("inMapping", extension["PROP_TEST3"])
        Assert.assertEquals("\${PROP_NOT_MAPPED}", extension["PROP_TEST4"])
    }


    /** 2) Check the "MutableMap<String, String>.substituteProperties" method: other map provided */
    @Test fun test_substituteProperties_otherMap() {
        val extension = mutableMapOf(
            "PROP_TEST1" to "TEST123",
            "PROP_TEST2" to "\${PROP_TEST1}",
            "PROP_TEST3" to "\${PROP_MAPPING_1}",
            "PROP_TEST4" to "\${PROP_OTHER_1}",
            "PROP_TEST5" to "\${PROP_NOT_MAPPED}"
        )

        // run substitution on extension (MutableMap<String, String>)
        extension.substituteProperties(mapOf("PROP_MAPPING_1" to "inMapping"), mapOf("PROP_OTHER_1" to "inOther"))

        Assert.assertEquals("TEST123", extension["PROP_TEST1"])
        Assert.assertEquals("TEST123", extension["PROP_TEST2"])
        Assert.assertEquals("inMapping", extension["PROP_TEST3"])
        Assert.assertEquals("inOther", extension["PROP_TEST4"])
        Assert.assertEquals("\${PROP_NOT_MAPPED}", extension["PROP_TEST5"])
    }


    /** 3) Check the "MutableMap<String, String>.overwriteEnvironmentVariablesSystemProperty" method: nothing */
    @Test fun test_overwriteEnvironmentVariablesSystemProperty_noEnvironmentVariablesNoSystemProperties() {
        val map = mutableMapOf("start" to "finish")

        map.overwriteEnvironmentVariablesSystemProperty()

        Assert.assertTrue(map.containsKey("start"))
        Assert.assertEquals("finish", map["start"])
    }


    /** 4) Check the "MutableMap<String, String>.overwriteEnvironmentVariablesSystemProperty" method: env variable */
    @Test fun test_overwriteEnvironmentVariablesSystemProperty_EnvironmentVariablesNoSystemProperties() {
        val map = mutableMapOf("start" to "finish")

        withEnvironmentVariable("${ManifestPlugin.PREFIX_DEFAULT}start", "motor").execute {
            Assert.assertTrue(System.getenv().containsKey("${ManifestPlugin.PREFIX_DEFAULT}start"))

            map.overwriteEnvironmentVariablesSystemProperty(ManifestPlugin.PREFIX_DEFAULT)

            Assert.assertTrue(map.containsKey("start"))
            Assert.assertEquals("motor", map["start"])
        }
    }


    /** 5) Check the "MutableMap<String, String>.overwriteEnvironmentVariablesSystemProperty" method: sys property */
    @Test fun test_overwriteEnvironmentVariablesSystemProperty_noEnvironmentVariablesSystemProperties() {
        val map = mutableMapOf("start" to "finish")

        restoreSystemProperties {
            System.setProperty("${ManifestPlugin.PREFIX_DEFAULT}start", "computer")
            Assert.assertTrue(System.getProperties().containsKey("${ManifestPlugin.PREFIX_DEFAULT}start"))

            map.overwriteEnvironmentVariablesSystemProperty(ManifestPlugin.PREFIX_DEFAULT)

            Assert.assertTrue(map.containsKey("start"))
            Assert.assertEquals("computer", map["start"])
        }
    }
}
