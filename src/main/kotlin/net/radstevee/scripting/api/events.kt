package net.radstevee.scripting.api

import net.radstevee.scripting.init.MCScriptingBukkit
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

inline fun <reified E : Event> event(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    crossinline listener: (E) -> Unit
) {
    println("Event function called")
    Bukkit.getPluginManager().registerEvent(
        E::class.java,
        object : Listener {},
        priority,
        { _, event ->
            listener(event as E)
        },
        MCScriptingBukkit.instance,
        ignoreCancelled
    )
}