/*
 * Filename	: KeycloakAuthenticator.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/10, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server.keycloak

import net.ruinnel.radius.server.Authenticator
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.authorization.client.Configuration
import org.keycloak.authorization.client.util.HttpResponseException
import org.slf4j.LoggerFactory

class KeycloakAuthenticator(
    private val secretKey: String,
    private val replyMessage: String,
    private val useOTP: Boolean,
    config: KeycloakConfig,
    private val permissionChecker: KeycloakPermissionChecker? = null
    ): Authenticator {
    private val logger = LoggerFactory.getLogger("main")
    private val authzClient: AuthzClient = AuthzClient.create(
        Configuration(
            config.authServerUrl,
            config.realm,
            config.resource,
            mapOf("secret" to config.credentials),
            null)
    )

    override fun authenticate(username: String, password: String, otp: String?): Boolean {
        try {
            val accessTokenResponse = authzClient.obtainAccessToken(username, password)
            return if (accessTokenResponse != null && accessTokenResponse.token != null && accessTokenResponse.token.isNotEmpty()) {
                permissionChecker?.checkPermission(authzClient, accessTokenResponse) ?: true
            } else {
                false
            }
        } catch (e: HttpResponseException) {
            logger.warn("authenticate fail - $username")
        }
        return true
    }

    override fun useOtp(nasIdentifier: String?): Boolean {
        return useOTP
    }

    override fun secretKey(): String {
        return secretKey
    }

    override fun replyMessage(): String {
        return replyMessage
    }
}