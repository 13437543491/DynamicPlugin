package com.quick.resid

import org.gradle.api.Plugin
import org.gradle.api.Project

class ResPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("ResPlugin")

        ResHandler resHandler = new ResHandler()

        project.afterEvaluate {
            project.android.applicationVariants.all { variant ->
                variant.outputs.each { variantOutput ->
                    def processResources = variantOutput.getProcessResources()
                    processResources.doLast {
                        File packageOutputFile = fetchPackageOutputFile(processResources)
                        println "packageOutputFile: " + packageOutputFile

                        File sourceOutputDir = fetchSourceOutputDirFile(processResources)
                        println "sourceOutputDir: " + sourceOutputDir

                        File textSymbolOutputDir = fetchTextSymbolOutputDir(processResources)
                        println "textSymbolOutputDir: " + textSymbolOutputDir

                        resHandler.processResId(packageOutputFile, sourceOutputDir, textSymbolOutputDir)
                    }
                }
            }
        }
    }

    private static File fetchPackageOutputFile(Object task) {
        if (task.properties['resPackageOutputFolder'] != null) {
            return asFile(task.resPackageOutputFolder)
        }

        if (task.properties['packageOutputFile'] != null) {
            return asFile(task.packageOutputFile)
        }

        return null
    }

    private static File fetchSourceOutputDirFile(Object task) {
        if (task.properties['sourceOutputDir'] != null) {
            return asFile(task.sourceOutputDir)
        }

        if (task.properties['RClassOutputJar'] != null) {
            return asFile(task.RClassOutputJar)
        }

        return null
    }

    static File fetchTextSymbolOutputDir(Object task) {
        try {
            return asFile(task.getTextSymbolOutputFile())
        } catch (Exception e) {
            e.printStackTrace()
        }

        if (task.properties['textSymbolOutputDir'] != null) {
            return task.textSymbolOutputDir
        }

        return null
    }

    private static File asFile(Object input) {
        try {
            return (File) input;
        } catch (Exception e) {
            println(e)
        }

        return input.getAsFile().get()
    }
}