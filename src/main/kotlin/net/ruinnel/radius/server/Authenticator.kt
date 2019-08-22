/*
 * Filename	: Authenticator.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/08, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server

interface Authenticator {
  fun authenticate(username: String, password: String, otp: String?): Boolean
  fun useOtp(nasIdentifier: String?): Boolean
  fun secretKey(): String
  fun replyMessage(): String
}