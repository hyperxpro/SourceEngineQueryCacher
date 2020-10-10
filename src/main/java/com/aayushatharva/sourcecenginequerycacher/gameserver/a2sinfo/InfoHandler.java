package com.aayushatharva.sourcecenginequerycacher.gameserver.a2sinfo;

import com.aayushatharva.sourcecenginequerycacher.utils.CacheHub;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class InfoHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LogManager.getLogger(InfoHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        // Set new Packet Data
        CacheHub.A2S_INFO.clear();
        CacheHub.A2S_INFO.writeBytes(byteBuf);

        logger.debug("New A2SInfo Update Cached Successfully");
    }
}
