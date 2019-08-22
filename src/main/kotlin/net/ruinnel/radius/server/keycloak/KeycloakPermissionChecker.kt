/*
 * Filename	: KeycloakPermissionChecker.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/10, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server.keycloak

import org.keycloak.authorization.client.AuthzClient
import org.keycloak.representations.AccessTokenResponse

interface KeycloakPermissionChecker {
    fun checkPermission(authzClient: AuthzClient, accessTokenResponse: AccessTokenResponse): Boolean
}