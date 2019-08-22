/*
 * Filename	: RadiusPacketProcessor.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/08, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server

import org.slf4j.LoggerFactory
import org.tinyradius.attribute.RadiusAttribute
import org.tinyradius.packet.AccessRequest
import org.tinyradius.packet.RadiusPacket
import org.tinyradius.util.RadiusServer
import java.net.InetSocketAddress

class RadiusPacketProcessor(
    private val authenticator :Authenticator,
    private val sharedSecret: String
) : RadiusServer() {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val codec = Codec(authenticator.secretKey())

    public override fun handlePacket(
        localAddress: InetSocketAddress?,
        remoteAddress: InetSocketAddress?,
        request: RadiusPacket?,
        sharedSecret: String?
    ): RadiusPacket? {
        return super.handlePacket(localAddress, remoteAddress, request, sharedSecret)
    }

    fun makeDatagramPacket(
        packet: RadiusPacket?,
        secret: String?,
        address: InetSocketAddress?,
        request: RadiusPacket?
    ): ByteArray {
        return super.makeDatagramPacket(packet, secret, address?.address, address?.port!!, request).data
    }

    override fun accessRequestReceived(accessRequest: AccessRequest, client: InetSocketAddress): RadiusPacket {
        val nasId = accessRequest.getAttribute("NAS-Identifier")?.attributeValue
        val username = accessRequest.userName
        var password = accessRequest.userPassword
        var otp = ""

        val state = accessRequest.getAttribute("State")?.attributeData

        var answer: RadiusPacket

        logger.debug("useOtp - ${authenticator.useOtp(nasId)}, otp - ${otp}")
        if (authenticator.useOtp(nasId)) {
            if (state != null && password != null) {
                otp = password
                password = String(codec.decrypt(state), Charsets.UTF_8)
            }

            // access challenge - request otp
            if (otp.isEmpty()) {
                answer = RadiusPacket(RadiusPacket.ACCESS_CHALLENGE, accessRequest.packetIdentifier)
                answer.addAttribute(RadiusAttribute(answer.dictionary.getAttributeTypeByName("State").typeCode, codec.encrypt(password!!))) // State
                answer.addAttribute("Reply-Message", authenticator.replyMessage())

                copyProxyState(accessRequest, answer)
                return answer
            }
        }

        logger.debug("accessRequestReceived - nasId: ${nasId} / username: ${username} / password: ${password} / otp: ${otp}")

        try {
            answer = if (authenticator.authenticate(username!!, password!!, otp)) {
                RadiusPacket(RadiusPacket.ACCESS_ACCEPT, accessRequest.packetIdentifier)
            } else {
                RadiusPacket(RadiusPacket.ACCESS_REJECT, accessRequest.packetIdentifier)
            }
        } catch (e: RuntimeException) {
            answer = RadiusPacket(RadiusPacket.ACCESS_REJECT, accessRequest.packetIdentifier)
            logger.warn("authorization fail - $username")
        }

        copyProxyState(accessRequest, answer)
        return answer
    }

    override fun getUserPassword(userName: String?): String {
        // never called.
        return ""
    }

    override fun getSharedSecret(client: InetSocketAddress?): String {
        return sharedSecret
    }
}