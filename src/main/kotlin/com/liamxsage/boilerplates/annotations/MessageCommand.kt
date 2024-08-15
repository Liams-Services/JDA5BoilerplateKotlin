package com.liamxsage.boilerplates.annotations

annotation class MessageCommand(
    val name: String,
    val description: String,
    val permissionScope: PermissionScope = PermissionScope.USER
)