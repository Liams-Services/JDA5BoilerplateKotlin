package one.devsky.boilerplates.listeners

import dev.fruxz.ascend.extension.logging.getItsLogger
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class JoinListener: ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        getItsLogger().info("User ${event.member.user.name} joined ${event.guild.name}")
        val defaultChannel = event.guild.defaultChannel?.asTextChannel() ?: return

        defaultChannel.sendMessage("Welcome ${event.member.asMention} to ${event.guild.name}").queue()
    }
}