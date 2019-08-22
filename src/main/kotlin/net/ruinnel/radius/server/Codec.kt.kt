/*
 * Filename	: Codec.kt.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/10, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Codec(key: String) {
  private val keySpec = SecretKeySpec(key.toByteArray(), "AES")
  private val iv = IvParameterSpec(key.substring(0, 16).toByteArray())
  private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

  fun encrypt(str: String): ByteArray {
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
    val encrypted = cipher.doFinal(str.toByteArray(Charsets.UTF_8))
    return encrypted
  }

  fun decrypt(encrypted: ByteArray): ByteArray {
    cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
    val decrypted = cipher.doFinal(encrypted)
    return decrypted
  }
}
