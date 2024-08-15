package com.liamxsage.boilerplates.manager

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import com.liamxsage.boilerplates.annotations.MessageCommand
import com.liamxsage.boilerplates.annotations.PermissionScope
import com.liamxsage.boilerplates.annotations.SlashCommand
import com.liamxsage.boilerplates.annotations.UserCommand
import com.liamxsage.klassicx.extensions.getLogger
import com.liamxsage.boilerplates.interfaces.HasOptions
import com.liamxsage.boilerplates.interfaces.HasSubcommandGroups
import com.liamxsage.boilerplates.interfaces.HasSubcommands
import com.liamxsage.klassicx.tools.Environment
import dev.fruxz.ascend.extension.forceCast
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.checkerframework.checker.units.qual.C
import org.reflections8.Reflections
import kotlin.time.measureTime

object RegisterManager {

    private var loadedClasses = mapOf<String, Any>()

    fun JDABuilder.registerAll() : JDABuilder {
        val reflections = Reflections("com.liamxsage.boilerplates")

        // Registering both ListenerAdapters and EventListeners
        val listenerTime = measureTime {
            for (clazz in (reflections.getSubTypesOf(ListenerAdapter::class.java) + reflections.getSubTypesOf(EventListener::class.java)).distinct()) {
                if (clazz.simpleName == "ListenerAdapter") continue

                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val listener = constructor.newInstance()
                loadedClasses += clazz.simpleName to listener
                addEventListeners(listener)
                getLogger().info("Registered listener: ${listener.javaClass.simpleName}")
            }
        }
        getLogger().info("Registered listeners in $listenerTime")

        return this
    }

    fun JDA.registerCommands(): JDA {
        val reflections = Reflections("com.liamxsage.boilerplates")

        // Registering commands
        val commandsTime = measureTime {
            registerSlashCommands(reflections)

            // UserCommands
            registerUserCommands(reflections)

            // MessageCommands
            registerMessageCommands(reflections)
        }
        getLogger().info("Registered commands in $commandsTime")

        return this
    }

    private fun JDA.registerMessageCommands(
        reflections: Reflections
    ) {
        for (clazz in reflections.getTypesAnnotatedWith(MessageCommand::class.java)) {
            val annotation = clazz.getAnnotation(MessageCommand::class.java)
            val data = Commands.message(annotation.name)
                .setPermissionScope<CommandData>(annotation.permissionScope)

            if (clazz.simpleName !in loadedClasses) {
                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val command = constructor.newInstance()
                loadedClasses += clazz.simpleName to command
                getLogger().info("Registered message command class: ${command.javaClass.simpleName}")
            }

            upsertCommand(data).queue()
            getLogger().info("Registered global message command: ${annotation.name}")
        }
    }

    private fun JDA.registerUserCommands(
        reflections: Reflections,
    ) {
        for (clazz in reflections.getTypesAnnotatedWith(UserCommand::class.java)) {
            val annotation = clazz.getAnnotation(UserCommand::class.java)
            val data = Commands.user(annotation.name)
                .setPermissionScope<CommandData>(annotation.permissionScope)

            if (clazz.simpleName !in loadedClasses) {
                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val command = constructor.newInstance()
                loadedClasses += clazz.simpleName to command
                getLogger().info("Registered user command class: ${command.javaClass.simpleName}")
            }

            upsertCommand(data).queue()
            getLogger().info("Registered global user command: ${annotation.name}")
        }
    }

    private fun JDA.registerSlashCommands(reflections: Reflections) {
        for (clazz in reflections.getTypesAnnotatedWith(SlashCommand::class.java)) {
            val annotation = clazz.getAnnotation(SlashCommand::class.java)
            val data = Commands.slash(annotation.name, annotation.description)
                .setPermissionScope<SlashCommandData>(annotation.permissionScope)

            if (clazz.simpleName !in loadedClasses) {
                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val command = constructor.newInstance()
                loadedClasses += clazz.simpleName to command
                getLogger().info("Registered command class: ${command.javaClass.simpleName}")
            }

            val command = loadedClasses[clazz.simpleName]
            addCommandDataOptions(command, data)

            upsertCommand(data).queue()
            getLogger().info("Registered global command: ${annotation.name}")
        }
    }

    private fun addCommandDataOptions(command: Any?, data: SlashCommandData) {
        if (command is HasOptions) {
            data.addOptions(command.getOptions())
        }

        if (command is HasSubcommandGroups) {
            data.addSubcommandGroups(command.getChoices())
        }

        if (command is HasSubcommands) {
            data.addSubcommands(command.getSubCommands())
        }
    }

    private fun <T : CommandData> CommandData.setPermissionScope(permissionScope: PermissionScope): T {
        when (permissionScope) {
            PermissionScope.MODERATOR -> {
                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
            }

            PermissionScope.ADMIN -> {
                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
            }

            else -> {
                /** Do Nothing  **/
            }
        }
        return this.forceCast()
    }
}