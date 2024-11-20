package net.radstevee.scripting.util

import org.bukkit.plugin.java.JavaPlugin
import java.io.File

object BukkitUtil {
    private val GET_FILE_METHOD = JavaPlugin::class.java.getDeclaredMethod("getFile")
    private val cache = mutableMapOf<String, File>()

    init {
        GET_FILE_METHOD.isAccessible = true
    }

    fun JavaPlugin.getJarFile(): File {
        cache[name]?.let { return it }
        val file = GET_FILE_METHOD.invoke(this) as File
        cache[name] = file
        return file
    }
}
