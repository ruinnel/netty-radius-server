/*
 * Filename	: Config.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/10, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server.keycloak

data class KeycloakConfig(
    val authServerUrl: String,
    val realm: String,
    val resource: String,
    val credentials: String
)