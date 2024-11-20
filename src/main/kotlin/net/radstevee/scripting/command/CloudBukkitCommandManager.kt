package net.radstevee.scripting.command

import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.bukkit.BukkitCommandManager
import org.incendo.cloud.execution.ExecutionCoordinator.simpleCoordinator

class CloudBukkitCommandManager(owningPlugin: JavaPlugin) :
    BukkitCommandManager<BukkitCommandSender>(owningPlugin, simpleCoordinator(), SenderMapper.create({ BukkitCommandSender(it) }, { it.bukkit }))