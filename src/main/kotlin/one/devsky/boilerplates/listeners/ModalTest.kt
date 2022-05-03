package one.devsky.boilerplates.listeners

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import one.devsky.boilerplates.annotations.SlashCommand


@SlashCommand("modal", "Erzeugt ein Test Modal", guilds = ["828274529070612539"])
class ModalTest : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if(name != "modal") return@with

        val email = TextInput.create("email", "Email", TextInputStyle.SHORT)
            .setPlaceholder("Enter your E-mail")
            .setMinLength(10)
            .setMaxLength(100) // or setRequiredRange(10, 100)
            .build()

        val body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Your concerns go here")
            .setMinLength(30)
            .setMaxLength(1000)
            .build()

        val modal: Modal = Modal.create("modal", "Support")
            .addActionRows(ActionRow.of(email), ActionRow.of(body))
            .build()

        replyModal(modal).queue()
    }


    override fun onModalInteraction(event: ModalInteractionEvent) = with(event) {
        if(modalId != "modal") return@with

        val email = getValue("email")?.asString
        val body = getValue("body")?.asString

        reply("Your email is $email and your body is $body").setEphemeral(true).queue()
    }
}