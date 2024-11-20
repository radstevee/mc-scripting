package net.radstevee.scripting.command

class BukkitCommandSender(val bukkit: org.bukkit.command.CommandSender) : CommandSender {
    override fun sendMessage(message: String) {
        bukkit.sendMessage(message)
    }
}