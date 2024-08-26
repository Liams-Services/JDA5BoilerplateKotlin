package com.liamxsage.boilerplates

import com.liamxsage.boilerplates.manager.RegisterManager.registerAll
import com.liamxsage.boilerplates.manager.RegisterManager.registerCommands
import com.liamxsage.klassicx.extensions.getLogger
import com.liamxsage.klassicx.tools.Environment
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

class JDA5Boilerplate {

    companion object {
        lateinit var instance: JDA5Boilerplate
    }

    private val jda: JDA

    init {
        instance = this

        jda = JDABuilder.createDefault(Environment.getString("BOT_TOKEN"))
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .registerAll()
            .build()
            .awaitReady()
            .registerCommands()

        getLogger().info("Bot is ready! ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }
}