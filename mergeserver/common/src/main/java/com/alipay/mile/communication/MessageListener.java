/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

/**
 * @author jin.qian
 * @version $Id: MessageListener.java,v 0.1 2011-4-6 ����10:45:12 jin.qian Exp $
 * �����첽֪ͨ�¼�
 * �����Ҫ������Ϣ����Ҫʵ������ӿڣ�����ʵ����ע�뵽MessageManager�С�
 */
public interface MessageListener {

    /**
     * �յ���Ϣ����Ľӿ�
     * @param data �յ������ݰ�
     * @param channel ͨ���õ�channel
     * @throws IOException
     */
    public void receiveMessage(byte[] data, Channel channel, long messageStartTime)
                                                                                   throws IOException;

    /**
     * �쳣����
     * @param e Netty ��װ�� �쳣
     */
    public void handleException(ExceptionEvent e);

}
