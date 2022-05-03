package one.devsky.boilerplates.manager

import de.moltenKt.core.extension.logging.getLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.boilerplates.annotations.SlashCommand
import org.reflections8.Reflections
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object RegisterManager {

    @OptIn(ExperimentalTime::class)
    fun JDABuilder.registerAll() : JDABuilder {
        val reflections = Reflections("one.devsky.boilerplates")

        // Registering both ListenerAdapters and EventListeners
        val listenerTime = measureTime {
            for (clazz in (reflections.getSubTypesOf(ListenerAdapter::class.java) + reflections.getSubTypesOf(EventListener::class.java)).distinct()) {
                if (clazz.simpleName == "ListenerAdapter") continue

                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val listener = constructor.newInstance()
                addEventListeners(listener)
                getLogger(RegisterManager::class).info("Registered listener: ${listener.javaClass.simpleName}")
            }
        }
        getLogger(RegisterManager::class).info("Registered listeners in $listenerTime")

        return this
    }

    @OptIn(ExperimentalTime::class)
    fun JDA.registerCommands(): JDA {
        val reflections = Reflections("one.devsky.boilerplates")

        // Registering commands
        val commandsTime = measureTime {
            for (clazz in reflections.getTypesAnnotatedWith(SlashCommand::class.java)) {
                val annotation = clazz.getAnnotation(SlashCommand::class.java)

                if(annotation.globalCommand) {
                    upsertCommand(annotation.name, annotation.description).queue()
                    getLogger(RegisterManager::class).info("Registered global command: ${annotation.name}")
                } else {
                    for (guildID in annotation.guilds) {
                        getGuildById(guildID)?.let { guild ->
                            guild.upsertCommand(annotation.name, annotation.description).queue()
                            getLogger(RegisterManager::class).info("Registered command: ${annotation.name} in guild: ${guild.name}")
                        }
                    }
                }
            }
        }
        getLogger(RegisterManager::class).info("Registered commands in $commandsTime")

        return this
    }
}