/*
 * Filename	: Application.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/07, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server

import net.ruinnel.radius.server.keycloak.KeycloakAuthenticator
import net.ruinnel.radius.server.keycloak.KeycloakConfig

const val defaultNettyThreadCount = "0"
const val defaultRadiusAuthPort = "1812"
const val defaultRadiusAcctPort = "1813"
const val defaultRadiusSharedSecret = "8767CBCA-E9E0-4D86-9761-CCBADC58B167"

const val defaultSecretKey = "39CB23BB8E5B47198E84B3939C9D96CD" // 32 byte
const val defaultReplyMessage = "Verification Code: "
const val defaultUseOTP = "true"

const val defaultKeycloakAuthServerUrl = "http://localhost:8080/auth"
const val defaultKeycloakRealm = "radius"
const val defaultKeycloakClientId = "radius-server"
const val defaultKeycloakClientSecret = "e8f5b182-09de-4f21-92a3-8c9c18a74a4f"


fun getEnv(name: String, defaultValue: String): String {
  val value = System.getenv(name)
  return if (value != null && value.isNotEmpty()) {
    value
  } else {
    defaultValue
  }
}

fun main() {
  // load config
  val nettyThreadCount = getEnv("NETTY_THREAD_COUNT", defaultNettyThreadCount).toInt()
  val radiusAuthPort = getEnv("RADIUS_AUTH_PORT", defaultRadiusAuthPort).toInt()
  val radiusAcctPort = getEnv("RADIUS_ACCT_PORT", defaultRadiusAcctPort).toInt()
  val radiusSharedSecret = getEnv("RADIUS_SHARED_SECRET", defaultRadiusSharedSecret)
  val secretKey = getEnv("SECRET_KEY", defaultSecretKey)
  val replyMessage = getEnv("REPLY_MESSAGE", defaultReplyMessage)
  val useOtp = getEnv("USE_OTP", defaultUseOTP).toBoolean()

  val keycloakAuthServerUrl = getEnv("KEYCLOAK_URL", defaultKeycloakAuthServerUrl)
  val keycloakRealm = getEnv("KEYCLOAK_REALM", defaultKeycloakRealm)
  val keycloakClientId = getEnv("KEYCLOAK_CLIENT_ID", defaultKeycloakClientId)
  val keycloakClientSecret = getEnv("KEYCLOAK_CLIENT_SECRET", defaultKeycloakClientSecret)

  val keycloakConfig = KeycloakConfig(keycloakAuthServerUrl, keycloakRealm, keycloakClientId, keycloakClientSecret)
  val authenticator = KeycloakAuthenticator(secretKey, replyMessage, useOtp, keycloakConfig)

  // start
  NettyRadiusServer(
    nettyThreadCount,
    radiusAuthPort,
    radiusAcctPort,
    radiusSharedSecret,
    authenticator
  ).start()
}