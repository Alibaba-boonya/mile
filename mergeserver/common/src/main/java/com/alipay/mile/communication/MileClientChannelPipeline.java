/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.DefaultChannelPipeline;

/**
 * @author jin.qian
 * @version $Id: MileClientChannelPipeline.java,v 0.1 2011-4-6 ����02:44:58 jin.qian Exp $
 * Client ��ʼ�������
 */
public class MileClientChannelPipeline extends DefaultChannelPipeline {

    /** SendFuture���󼯺�����ͨ�ŷ���ʱ�� ��������Ҹ�ֵ */
    private Map<Integer, SendFuture> sendDataHandles;
    /**����server�б�  */
    private List<ServerRef>          serversOk;
    /**������״̬��server�б�  */
    private List<ServerRef>          serversFail;

    /**
     * @param sendDataHandles
     */
    public void setSendDataHandles(Map<Integer, SendFuture> sendDataHandles) {
        this.sendDataHandles = sendDataHandles;
    }

    /**
     * @return
     */
    public Map<Integer, SendFuture> getSendDataHandles() {
        return sendDataHandles;
    }

    /**
     * @return
     */
    public List<ServerRef> getServersOk() {
        return serversOk;
    }

    /**
     * @param mergeServersOk
     */
    public void setServersOk(List<ServerRef> serversOk) {
        this.serversOk = serversOk;
    }

    /**
     * @return
     */
    public List<ServerRef> getServersFail() {
        return serversFail;
    }

    /**
     * @param mergeServersFail
     */
    public void setServersFail(List<ServerRef> serversFail) {
        this.serversFail = serversFail;
    }
}
