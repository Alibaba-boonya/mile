/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

/**
 * @author jin.qian
 * @version $Id: MessageClientPipelineFactory.java,v 0.1 2011-4-6 ����10:34:46 jin.qian Exp $
 * ͨѶ�� �ͻ��˳�ʼ��������ֵ
 * 
 */
public class MessageClientPipelineFactory implements ChannelPipelineFactory {

    private static final Logger      LOGGER = Logger.getLogger(MessageClientPipelineFactory.class
                                                .getName());

    /** SendFuture���󼯺�����ͨ�ŷ���ʱ�� ��������Ҹ�ֵ */
    private Map<Integer, SendFuture> sendDataHandles;

    /**����server�б�  */
    private List<ServerRef>          serversOk;

    /**������״̬��server�б�  */
    private List<ServerRef>          serversFail;

    /**
     * @param serversOk ����server�б�
     * @param serversFail ������server�б�
     * @param sendDataHandles SendFuture����
     */
    MessageClientPipelineFactory(List<ServerRef> serversOk, List<ServerRef> serversFail,
                                 Map<Integer, SendFuture> sendDataHandles) {
        super();
        this.sendDataHandles = sendDataHandles;
        this.serversOk = serversOk;
        this.serversFail = serversFail;
    }

    /** 
     * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
     */
    @Override
    public ChannelPipeline getPipeline() {
        //�����������ʵ���˹�������ģʽChannelPipeline
        MileClientChannelPipeline pipeline = new MileClientChannelPipeline();
        //��ӽ�����
        pipeline.addLast("decoder", new MileMessageDecoder());
        //��ӱ�����
        pipeline.addLast("encoder", new MileMessageEncoder());
        //��� �¼�������
        pipeline.addLast("handler", new MileClientHandler());
        //���ò����÷������б�
        pipeline.setServersFail(serversFail);
        //���ÿ��÷������б�
        pipeline.setServersOk(serversOk);
        //����SendFuture���󼯺�
        pipeline.setSendDataHandles(sendDataHandles);
        if (LOGGER.isInfoEnabled()) {
            LOGGER
                .info("���� ->MileMessageDecoder,MileMessageEncoder, MileClientHandler,MergeServersFail,MergeServersOk");
        }
        return pipeline;
    }

    public Map<Integer, SendFuture> getSendDataHandles() {
        return sendDataHandles;
    }

    public void setSendDataHandles(Map<Integer, SendFuture> sendDataHandles) {
        this.sendDataHandles = sendDataHandles;
    }

    public List<ServerRef> getServersOk() {
        return serversOk;
    }

    public void setServersOk(List<ServerRef> serversOk) {
        this.serversOk = serversOk;
    }

    public List<ServerRef> getServersFail() {
        return serversFail;
    }

    public void setServersFail(List<ServerRef> serversFail) {
        this.serversFail = serversFail;
    }
}
