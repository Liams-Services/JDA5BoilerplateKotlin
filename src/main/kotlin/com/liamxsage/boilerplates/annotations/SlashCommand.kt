package com.liamxsage.boilerplates.annotations

annotation class SlashCommand(
    val name: String,
    val description: String,
    val globalCommand: Boolean = true,
    val guilds: Array<String> = []
)
