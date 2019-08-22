/*
 * Filename	: RadiusServerHandler.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/07, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.socket.DatagramPacket
import org.slf4j.LoggerFactory
import org.tinyradius.packet.RadiusPacket
import org.tinyradius.util.RadiusException
import java.io.ByteArrayInputStream
import java.io.IOException

class RadiusServerHandler(
  authenticator: Authenticator,
  private val sharedSecret: String
) : ChannelInboundHandlerAdapter() {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val processor = RadiusPacketProcessor(authenticator, sharedSecret)

  override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
    try {
      // check client
      if (msg is DatagramPacket) {
        val localAddress = msg.recipient()
        val remoteAddress = msg.sender()
        val secret = sharedSecret

        // parse packet
        val content = msg.content()
        val array = if (content.hasArray()) {
          content.array()
        } else {
          val bytes = ByteArray(content.readableBytes())
          content.getBytes(content.readerIndex(), bytes)
          bytes
        }
        val inputStream = ByteArrayInputStream(array)
        val request = RadiusPacket.decodeRequestPacket(inputStream, secret, RadiusPacket.UNDEFINED)
        logger.debug("received packet from $remoteAddress on local address $localAddress: $request")

        // handle packet
        logger.debug("about to call RadiusServer.handlePacket()")
        val response = processor.handlePacket(localAddress, remoteAddress, request, secret)

        logger.debug("RadiusServer.handlerPacket() - ${response.toString()}")
        // send response
        if (response != null) {
          val packetOutBytes = processor.makeDatagramPacket(response, secret, remoteAddress, request)
          val packetOut = DatagramPacket(Unpooled.copiedBuffer(packetOutBytes), remoteAddress)
          ctx?.writeAndFlush(packetOut)
        } else {
          logger.debug("no response sent")
        }
      }
    } catch (ioe: IOException) {
      logger.warn("communication error", ioe)
    } catch (re: RadiusException) {
      // malformed packet
      logger.warn("malformed Radius packet", re)
    }
  }
}

