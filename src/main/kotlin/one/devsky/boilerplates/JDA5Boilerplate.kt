package one.devsky.boilerplates

import de.moltenKt.core.extension.logging.getLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import one.devsky.boilerplates.manager.RegisterManager.registerAll
import one.devsky.boilerplates.manager.RegisterManager.registerCommands
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class JDA5Boilerplate {

    companion object {
        lateinit var instance: JDA5Boilerplate
    }

    private val jda: JDA

    val properties = Properties()

    init {
        instance = this

        val propertiesFile = "data.properties"
        val file = File(propertiesFile)

        if (!file.exists()) {
            saveProperties()
        }

        val inputStream = FileInputStream(propertiesFile)
        properties.load(inputStream)

        jda = JDABuilder.createDefault(properties.getProperty("BOT_TOKEN", System.getenv("BOT_TOKEN")))
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger(JDA5Boilerplate::class).info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }

    fun saveProperties() {
        val file = File("data.properties")
        val fileOutputStream = FileOutputStream(file)
        properties.store(fileOutputStream, "Einstellungsdatei der JDA5Boilerplate")
    }

}