/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * @author jin.qian
 * @version $Id: MessageServerPipelineFactory.java,v 0.1 2011-4-6 ����10:56:28 jin.qian Exp $
 * ͨѶ�� ����˳�ʼ��������ֵ
 */
public class MessageServerPipelineFactory implements ChannelPipelineFactory {

    private static final Logger LOGGER = Logger.getLogger(MessageServerPipelineFactory.class
                                           .getName());

    /** ��Ϣ������ */
    private MessageManager      messageManager;

    /**
     * @param messageManager ��Ϣ������
     */
    public MessageServerPipelineFactory(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    /**
     * @return ������pipeline
     * @throws Exception
     * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
     */
    @Override
    public ChannelPipeline getPipeline() {
        //�����������ʵ���˹�������ģʽChannelPipeline
        ChannelPipeline pipeline = new MileServerChannelPipeline(messageManager);
        //��ӽ�����
        pipeline.addLast("decoder", new MileMessageDecoder());
        //��ӱ�����
        pipeline.addLast("encoder", new MileMessageEncoder());
        //��� �¼�������
        pipeline.addLast("handler", new MileServerHandler());
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("���� ->MileMessageDecoder,MileMessageEncoder,MileServerHandler");
        }
        return pipeline;
    }

    /**
     * ������Ϣ������
     * @param messageManager
     */
    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    /**
     * ȡ����Ϣ������
     * @return MessageManager
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

}
