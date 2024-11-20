package net.radstevee.scripting.init

import net.radstevee.scripting.command.BukkitCommandSender
import net.radstevee.scripting.command.CloudBukkitCommandManager
import net.radstevee.scripting.script.BukkitScript
import net.radstevee.scripting.script.host.evalFile
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.CommandManager
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

class MCScriptingBukkit : MCScriptingInstance<BukkitCommandSender>, JavaPlugin() {
    override fun onEnable() {
        instance = this
        scriptsDirectory = File(dataFolder, "scripts")
        scriptsDirectory.mkdirs()

        commandManager = CloudBukkitCommandManager(this)

        initCommands()
    }

    override lateinit var commandManager: CommandManager<BukkitCommandSender>
    override lateinit var scriptsDirectory: File

    override fun executeScript(sender: BukkitCommandSender, script: File) {
        val res = evalFile<BukkitScript>(script)
        res.reports.forEach { report ->
            if (report.severity > ScriptDiagnostic.Severity.DEBUG) {
                sender.sendMessage("${report.severity.name}: ${report.message}" + if (report.exception == null) "" else ": ${report.exception}")
            }
        }
    }

    companion object {
        lateinit var instance: MCScriptingBukkit
    }
}