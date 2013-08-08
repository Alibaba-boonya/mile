/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * @author jin.qian
 * @version $Id: MileMessageDecoder.java,v 0.1 2011-4-6 ����04:57:49 jin.qian Exp $
 * ������
 */
public class MileMessageDecoder extends FrameDecoder {

    private static final Logger LOGGER = Logger.getLogger(MileMessageDecoder.class.getName());

    /** 
     * @see org.jboss.netty.handler.codec.frame.FrameDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer)
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
        //��Ϣ���Ȳ��� ����
        if (buffer.readableBytes() < 4) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("��Ϣ���Ȳ��� ");
            }
            return null;
        }
        //��Ϣ����
        int dataLength = buffer.getInt(buffer.readerIndex());
        //��Ϣû������ ���ؼ�������
        if (buffer.readableBytes() < dataLength) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("��Ϣû������ ���ؼ������� ");
            }
            return null;
        }
        //��ȡ������Ϣ
        byte[] decoded = new byte[dataLength];
        buffer.readBytes(decoded);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("��ȡ������Ϣ ");
        }
        return decoded;
    }

}
