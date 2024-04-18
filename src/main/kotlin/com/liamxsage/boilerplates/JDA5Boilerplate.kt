package com.liamxsage.boilerplates

import dev.fruxz.ascend.extension.logging.getItsLogger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import com.liamxsage.boilerplates.manager.RegisterManager.registerAll
import com.liamxsage.boilerplates.manager.RegisterManager.registerCommands
import com.liamxsage.boilerplates.utils.Environment

class JDA5Boilerplate {

    companion object {
        lateinit var instance: JDA5Boilerplate
    }

    private val jda: JDA

    init {
        instance = this

        jda = JDABuilder.createDefault(Environment.getEnv("BOT_TOKEN"))
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getItsLogger().info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }
}