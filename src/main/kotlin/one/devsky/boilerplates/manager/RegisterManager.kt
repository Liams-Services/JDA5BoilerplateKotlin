package one.devsky.boilerplates.manager

import de.moltenKt.core.extension.logging.getLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import one.devsky.boilerplates.JDA5Boilerplate
import one.devsky.boilerplates.annotations.MessageCommand
import one.devsky.boilerplates.annotations.SlashCommand
import one.devsky.boilerplates.annotations.UserCommand
import one.devsky.boilerplates.interfaces.HasSubcommandGroups
import one.devsky.boilerplates.interfaces.HasOptions
import one.devsky.boilerplates.interfaces.HasSubcommands
import org.reflections8.Reflections
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object RegisterManager {

    private var loadedClasses = mapOf<String, Any>()

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
                loadedClasses += clazz.simpleName to listener
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
        val guildIds = JDA5Boilerplate.instance.properties.getProperty("guilds", "").split(",").toTypedArray()

        // Registering commands
        val commandsTime = measureTime {
            for (clazz in reflections.getTypesAnnotatedWith(SlashCommand::class.java)) {
                val annotation = clazz.getAnnotation(SlashCommand::class.java)
                val data = Commands.slash(annotation.name, annotation.description)

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger(RegisterManager::class).info("Registered command class: ${command.javaClass.simpleName}")
                }

                val command = loadedClasses[clazz.simpleName]

                if (command is HasOptions) {
                    data.addOptions(command.getOptions())
                }

                if (command is HasSubcommandGroups) {
                    data.addSubcommandGroups(command.getChoices())
                }

                if (command is HasSubcommands) {
                    data.addSubcommands(command.getSubCommands())
                }

                if(annotation.globalCommand) {
                    upsertCommand(data).queue()
                    getLogger(RegisterManager::class).info("Registered global command: ${annotation.name}")
                } else {
                    for (guildID in annotation.guilds) {
                        getGuildById(guildID)?.let { guild ->
                            guild.upsertCommand(data).queue()
                            getLogger(RegisterManager::class).info("Registered command: ${annotation.name} in guild: ${guild.name}")
                        }
                    }
                }
            }

            // UserCommands
            for (clazz in reflections.getTypesAnnotatedWith(UserCommand::class.java)) {
                val annotation = clazz.getAnnotation(UserCommand::class.java)
                val data = Commands.user(annotation.name)

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger(RegisterManager::class).info("Registered user command class: ${command.javaClass.simpleName}")
                }

                if(annotation.globalCommand) {
                    upsertCommand(data).queue()
                    getLogger(RegisterManager::class).info("Registered global user command: ${annotation.name}")
                } else {
                    for (guildID in (guildIds + annotation.guilds).distinct().filterNot { it.isEmpty() }) {
                        getGuildById(guildID)?.let { guild ->
                            guild.upsertCommand(data).queue()
                            getLogger(RegisterManager::class).info("Registered user command: ${annotation.name} in guild: ${guild.name}")
                        }
                    }
                }
            }


            // MessageCommands
            for (clazz in reflections.getTypesAnnotatedWith(MessageCommand::class.java)) {
                val annotation = clazz.getAnnotation(MessageCommand::class.java)
                val data = Commands.message(annotation.name)

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger(RegisterManager::class).info("Registered message command class: ${command.javaClass.simpleName}")
                }

                if(annotation.globalCommand) {
                    upsertCommand(data).queue()
                    getLogger(RegisterManager::class).info("Registered global message command: ${annotation.name}")
                } else {
                    for (guildID in (guildIds + annotation.guilds).distinct().filterNot { it.isEmpty() }) {
                        getGuildById(guildID)?.let { guild ->
                            guild.upsertCommand(data).queue()
                            getLogger(RegisterManager::class).info("Registered message command: ${annotation.name} in guild: ${guild.name}")
                        }
                    }
                }
            }
        }
        getLogger(RegisterManager::class).info("Registered commands in $commandsTime")

        return this
    }
}