/*
 * Filename	: NettyRadiusServer.kt
 * Function	:
 * Comment 	:
 * History	: 2019/08/10, ruinnel, Create
 *
 * Version	: 1.0
 * Author   : Copyright (c) 2019 by JC Square Inc. All Rights Reserved.
 */

package net.ruinnel.radius.server

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.slf4j.LoggerFactory


class NettyRadiusServer(
    private val nettyThreadCount: Int,
    private val radiusAuthPort: Int = 1812,
    private val radiusAcctPort: Int = 1813,
    private val radiusSharedSecret: String,
    private val authenticator: Authenticator
) {
    private val logger = LoggerFactory.getLogger(NettyRadiusServer::class.simpleName)

    fun start() {
        val workerGroup = NioEventLoopGroup(nettyThreadCount)
        try {
            val bootstrap = Bootstrap()
            bootstrap.group(workerGroup)
                .channel(NioDatagramChannel::class.java)
                .handler(LoggingHandler(LogLevel.TRACE))
                .handler(object : ChannelInitializer<NioDatagramChannel>() {
                    override fun initChannel(ch: NioDatagramChannel?) {
                        val pipeline = ch?.pipeline()
                        pipeline?.addLast("handler", RadiusServerHandler(authenticator, radiusSharedSecret))
                    }
                })

            val channels = mutableListOf<Channel>()
            channels.add(bootstrap.bind(radiusAuthPort).sync().channel())
            channels.add(bootstrap.bind(radiusAcctPort).sync().channel())
            for (ch in channels) {
                ch.closeFuture().sync()
            }
        } catch (e: Exception) {
            logger.error("server error", e)
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}