package com.liamxsage.boilerplates.annotations

annotation class SlashCommand(
    val name: String,
    val description: String,
    val permissionScope: PermissionScope = PermissionScope.USER
)
