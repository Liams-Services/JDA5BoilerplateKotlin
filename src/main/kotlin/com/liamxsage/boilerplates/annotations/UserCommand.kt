package com.liamxsage.boilerplates.annotations

annotation class UserCommand(
    val name: String,
    val description: String,
    val permissionScope: PermissionScope = PermissionScope.USER
)