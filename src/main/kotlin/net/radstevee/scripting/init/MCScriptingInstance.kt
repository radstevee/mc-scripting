package net.radstevee.scripting.init

import net.radstevee.scripting.command.CommandSender
import org.incendo.cloud.CommandManager
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import java.io.File

interface MCScriptingInstance<C : CommandSender> {
    val commandManager: CommandManager<C>
    val scriptsDirectory: File
    fun executeScript(sender: C, script: File)

    fun initCommands() {
        val execCommand = commandManager
            .commandBuilder("kts")
            .literal("exec")
            //.permission("kts.command.exec")
            .required("fileName", quotedStringParser())
            .handler { ctx ->
                val scriptFile = ctx.get<String>("fileName")
                val file = File(scriptsDirectory, scriptFile)
                if (!file.exists()) {
                    ctx.sender().sendMessage("File '$file' does not exist!")
                    return@handler
                }

                executeScript(ctx.sender(), file)
            }

        commandManager.command(execCommand)
    }
}