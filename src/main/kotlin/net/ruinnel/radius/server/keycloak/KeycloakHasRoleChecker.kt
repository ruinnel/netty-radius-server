/*
 * Filename	: KeycloakPermissionCheckerImpl.kt
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
import org.keycloak.representations.idm.authorization.AuthorizationRequest
import org.tinyradius.packet.RadiusPacket

class KeycloakHasRoleChecker(
    private val resourceName: String
): KeycloakPermissionChecker {
    private var resourceId: String? = null

    private fun getResourceId(authzClient: AuthzClient): String {
        if (resourceId == null) {
            resourceId = authzClient.protection()
                .resource()
                .findByName(resourceName)
                .id
        }
        return resourceId!!
    }

    override fun checkPermission(authzClient: AuthzClient, accessTokenResponse: AccessTokenResponse): Boolean {
        val authzResource = authzClient.authorization(accessTokenResponse.token)
        val authzRequest = AuthorizationRequest()
        authzRequest.setRpt(accessTokenResponse.token)
        authzRequest.addPermission(getResourceId(authzClient))

        val authzResponse = authzResource.authorize(authzRequest)
        val result = authzClient.protection().introspectRequestingPartyToken(authzResponse.token)

        return result.active
    }
}