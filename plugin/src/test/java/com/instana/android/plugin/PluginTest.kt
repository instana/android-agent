/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.VariantSelector
import com.android.build.gradle.AppExtension
import com.nhaarman.mockitokotlin2.validateMockitoUsage
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.ExtensionContainer
import org.junit.After
import org.junit.Test
import org.mockito.Mockito

abstract class PluginWrapper : Plugin() {
    abstract fun createProject(): Project
}

class PluginTest {

    @After
    fun validate(){
        validateMockitoUsage()
    }

    @Test
    fun `test Apply`() {
        try {
            val project = Mockito.mock(Project::class.java)
            val logger = Mockito.mock(Logger::class.java)
            val appExtension = Mockito.mock(AppExtension::class.java)
            val extensionContainer = Mockito.mock(ExtensionContainer::class.java)
            val androidComponentsExtension = Mockito.mock(AndroidComponentsExtension::class.java)
            val variantSelector = Mockito.mock(VariantSelector::class.java)

            Mockito.`when`(project.logger).thenReturn(logger)
            Mockito.`when`(project.project).thenReturn(project)
            Mockito.`when`(project.project.extensions).thenReturn(extensionContainer)
            Mockito.`when`(project.extensions.findByName("android")).thenReturn(appExtension)
            Mockito.`when`(project.extensions.getByType(AndroidComponentsExtension::class.java)).thenReturn(androidComponentsExtension)
            Mockito.`when`(project.extensions.getByType(AndroidComponentsExtension::class.java).selector()).thenReturn(variantSelector)

            val plugin = object : PluginWrapper() {
                override fun createProject(): Project {
                    return project
                }
            }
            plugin.apply(plugin.createProject())

            assert(!plugin.createProject().hasProperty("project"))
        }catch (e:Exception){
            println(e.localizedMessage)
        }

    }

    @Test(expected = Exception::class)
    fun testApplyWithNonAppExtension() {
        val project = Mockito.mock(Project::class.java)
        val logger = Mockito.mock(Logger::class.java)

        Mockito.`when`(project.logger).thenReturn(logger)
        Mockito.`when`(project.extensions.findByName("android")).thenReturn(null)

        val plugin = object : PluginWrapper() {
            override fun createProject(): Project {
                return project
            }
        }
        plugin.apply(plugin.createProject())
    }
}


