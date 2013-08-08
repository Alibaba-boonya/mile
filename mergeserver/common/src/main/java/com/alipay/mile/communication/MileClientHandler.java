/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.alipay.mile.message.MergerDocHeartcheck;
import com.alipay.mile.message.Message;
import com.alipay.mile.message.MessageFactory;

/**
 * @author jin.qian
 * @version $Id: MileClientHandler.java,v 0.1 2011-4-6 ����04:49:59 jin.qian Exp $
 * �յ���Ϣ������
 */
public class MileClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger LOGGER = Logger.getLogger(MileClientHandler.class.getName());

    /**
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("��������: " + e.getChannel().getLocalAddress() + "-->"
                        + e.getChannel().getRemoteAddress());
        }
    }

    /**
     * @param ctx
     * @param e
     * @throws Exception
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     * �Ͽ����Ӻ��� �������б� ά��������״̬
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        //ȡ�����÷������б��ǿ��÷������б�
        MileClientChannelPipeline mileChannelPipeline = (MileClientChannelPipeline) e.getChannel()
            .getPipeline();
        List<ServerRef> serversOk = mileChannelPipeline.getServersOk();
        List<ServerRef> serversFail = mileChannelPipeline.getServersFail();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("�Ͽ�����: " + e.getChannel().getLocalAddress() + "-->"
                        + e.getChannel().getRemoteAddress());
        }
        //��ʶ������״̬
        for (ServerRef ms : serversOk) {
            if (ms.getChannel().getId().equals(e.getChannel().getId())) {
                ms.setAvailable(false);
                //����ǰ�������ӿ����б��Ƶ��ǿ����б�
                if (serversOk.remove(ms)) {
                    serversFail.add(ms);
                }
            }
        }
        //�Ͽ��������п����Ƿ��񲻿��õ�״̬������������÷������б�
        for (ServerRef ms : serversFail) {
            if (ms.getChannel().getId().equals(e.getChannel().getId())) {
                ms.setAvailable(false);
            }
        }

        //�¼��ϴ�
        ctx.sendUpstream(e);

    }

    /**
     * @param ctx
     * @param e
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
     * �յ���Ϣ ���� Future ����
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        byte[] data = (byte[]) e.getMessage();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("�յ���Ϣ  ID:" + MessageFactory.getMessageId(data));
        }
        if (MessageFactory.getMessageType(data) == Message.MT_MD_HEART) {
            MergerDocHeartcheck mergerDocHeartcheck = (MergerDocHeartcheck) MessageFactory
                .toMessage(data);
            if (mergerDocHeartcheck.getState() == 1) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("�յ�������");
                }
                mergerDocHeartcheck.setState((short) 2);
                try {
                    e.getChannel().write(mergerDocHeartcheck.toBytes());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Doc�������Ӧ��");
                    }
                } catch (IOException e1) {
                    LOGGER.error("Doc����������", e1);
                }
            }
        } else {
            MileClientChannelPipeline mileChannelPipeline = (MileClientChannelPipeline) e
                .getChannel().getPipeline();
            //ȡ�÷���handle
            SendFuture sendDataHandle = mileChannelPipeline.getSendDataHandles().get(
                MessageFactory.getMessageId(data));
            if (sendDataHandle != null) {
                //ת�����ݳ�message���� ��ֵ�����
                sendDataHandle.setResult(MessageFactory.toMessage(data));
                sendDataHandle.setResultTime(System.currentTimeMillis());
                synchronized (sendDataHandle) {
                    //�����첽�߳�
                    sendDataHandle.notifyAll();
                }
                //��� sendFuture
                mileChannelPipeline.getSendDataHandles().remove(MessageFactory.getMessageId(data));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("remove sendDataHandle:" + MessageFactory.getMessageId(data));
                }
            } else {
                LOGGER.error("sendDataHandle is Null--messageID: "
                             + MessageFactory.getMessageId(data));
            }
        }
    }

    /**
     * @param ctx
     * @param e
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        LOGGER.error("ͨ���쳣Զ�˵�ַ" + ctx.getChannel().getRemoteAddress(), e.getCause());
        ctx.sendUpstream(e);
    }
}
