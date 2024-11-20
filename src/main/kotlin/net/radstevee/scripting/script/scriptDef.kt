package net.radstevee.scripting.script

import kotlinx.coroutines.runBlocking
import net.radstevee.scripting.util.BukkitUtil.getJarFile
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.RefineScriptCompilationConfigurationHandler
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCollectedData
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.asDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.collectedAnnotations
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.CompoundDependenciesResolver
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.FileSystemDependenciesResolver
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.dependencies.resolveFromScriptSourceAnnotations
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvmhost.jsr223.Jsr223EvaluationConfigurationBuilder.Companion.invoke

abstract class Script

abstract class ScriptConfiguration(defaultImports: List<String>, classpath: List<File>) : ScriptCompilationConfiguration({
    defaultImports(DependsOn::class, Repository::class)
    defaultImports(
        "kotlin.*",
        "kotlin.annotations.*",
        "kotlin.collections.*",
        "kotlin.comparisons.*",
        "kotlin.io.*",
        "kotlin.ranges.*",
        "kotlin.sequences.*",
        "kotlin.text.*",
        "kotlin.jvm.*",
        "java.lang.*"
    )
    defaultImports(*defaultImports.toTypedArray())

    dependencies(classpath.map { JvmDependency(it) })

    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
        updateClasspath(classpath)
    }

    refineConfiguration {
        onAnnotations(DependsOn::class, Repository::class, handler = ScriptConfigurator(classpath))
    }

    compilerOptions("-jvm-target", "21")
})

class ScriptConfigurator(val classpath: List<File>) : RefineScriptCompilationConfigurationHandler {
    private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

    override operator fun invoke(context: ScriptConfigurationRefinementContext) = processAnnotations(context)

    private fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val diagnostics = arrayListOf<ScriptDiagnostic>()
        val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)
            ?.takeIf(List<*>::isNotEmpty)
            ?: return context.compilationConfiguration.asSuccess()
        val resolveResult = runCatching {
            runBlocking {
                resolver.resolveFromScriptSourceAnnotations(
                    annotations.filter { it.annotation is DependsOn || it.annotation is Repository }
                )
            }
        }.getOrElse { exception ->
            ResultWithDiagnostics.Failure(
                *diagnostics.toTypedArray(),
                exception.asDiagnostics(path = context.script.locationId)
            )
        }

        return resolveResult.onSuccess { resolvedClassPath ->
            ScriptCompilationConfiguration(context.compilationConfiguration) {
                updateClasspath(resolvedClassPath)
                updateClasspath(classpath)
            }.asSuccess()
        }
    }
}

@KotlinScript(
    fileExtension = ScriptFileExtensions.BUKKIT,
    compilationConfiguration = BukkitScriptConfiguration::class,
)
abstract class BukkitScript : Script()

class BukkitScriptConfiguration : ScriptConfiguration(
    listOf(
        "org.bukkit.*",
        "org.bukkit.event.*",
        "org.bukkit.event.player.*",
        "net.radstevee.scripting.api.*"
    ),
    Bukkit.getPluginManager().plugins.filterIsInstance<JavaPlugin>().map {
        it.getJarFile()
    }
)

object ScriptFileExtensions {
    const val BUKKIT = "bukkit.kts"
    const val PAPER = "paper.kts"
}