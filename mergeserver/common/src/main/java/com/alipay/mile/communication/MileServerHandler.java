/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.alipay.mile.message.Message;
import com.alipay.mile.message.MessageFactory;

/**
 * @author jin.qian
 * @version $Id: MileServerHandler.java,v 0.1 2011-4-6 ����05:10:46 jin.qian Exp $
 * ����˽�����Ϣ����
 */
public class MileServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger LOGGER = Logger.getLogger(MileServerHandler.class.getName());

    /**
     * @param ctx
     * @param e
     * @throws Exception
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("��������: " + e.getChannel().getLocalAddress() + "-->"
                        + e.getChannel().getRemoteAddress());
        }
        ctx.sendUpstream(e);
    }

    /**
     * @param ctx
     * @param e
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
     * ������Ϣ���� �������� messageListener
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        MileServerChannelPipeline ChannelPipeline = (MileServerChannelPipeline) e.getChannel()
            .getPipeline();
        List<MessageListener> messageListeners = ChannelPipeline.getMessageManager()
            .getMessagelisteners();
        Iterator<MessageListener> it = messageListeners.iterator();
        //������Ϣ������ ����Ϣ֪ͨ��ÿ����������
        while (it.hasNext()) {
            MessageListener messageListener = it.next();
            //��Ϣ���������������̳߳ض��д�����Ϣ
            byte[] data = (byte[]) e.getMessage();
            Short messageType = MessageFactory.getMessageType(data);
            if (messageType == Message.MT_CM_Q_SQL || messageType == Message.MT_CM_PRE_Q_SQL) {
                ChannelPipeline.getMessageManager().queryExec.submit(new ReceiveDataHandle(data, e
                    .getChannel(), messageListener));
            } else {
                ChannelPipeline.getMessageManager().exec.submit(new ReceiveDataHandle(data, e
                    .getChannel(), messageListener));
            }
        }

    }

    /**
     * @param ctx
     * @param e
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     * �쳣�ַ�������MessageListener
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        MileServerChannelPipeline ChannelPipeline = (MileServerChannelPipeline) e.getChannel()
            .getPipeline();
        List<MessageListener> messageListeners = ChannelPipeline.getMessageManager()
            .getMessagelisteners();
        //�쳣�¼�֪ͨ�� messageListeners
        for (Iterator<MessageListener> messageListenerIterator = messageListeners.iterator(); messageListenerIterator
            .hasNext();) {
            MessageListener messageListener = messageListenerIterator.next();
            messageListener.handleException(e);
        }
        LOGGER.warn("Merge�����ͨ���쳣" + ctx.getChannel().getRemoteAddress(), e.getCause());
        ctx.sendUpstream(e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("�Ͽ�����: " + e.getChannel().getLocalAddress() + "-->"
                        + e.getChannel().getRemoteAddress());
        }
        //�Ͽ�����
        e.getChannel().close();
        //�¼��ϴ�
        ctx.sendUpstream(e);
    }
}
