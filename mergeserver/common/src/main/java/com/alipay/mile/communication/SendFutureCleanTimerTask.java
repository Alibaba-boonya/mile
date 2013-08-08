/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author jin.qian
 * @version $Id: ChannelPipelLineTimerTask.java,v 0.1 2011-4-6 ����10:19:18 jin.qian Exp $
 * ChannelPipelLineTimerTask �Ƕ�ʱ�������� ��Ҫ�����������񷵻ؽ�����ճɵ� SendFuture ��������
 *
 */
public class SendFutureCleanTimerTask implements Runnable {

    private static final Logger      LOGGER = Logger.getLogger(SendFutureCleanTimerTask.class
                                                .getName());
    private Map<Integer, SendFuture> sendDataHandles;

    SendFutureCleanTimerTask(Map<Integer, SendFuture> sendDataHandles) {
        this.sendDataHandles = sendDataHandles;
    }

    /**
     * ��ʱ����ʱ�� sendFuture
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + " ���ȿ�ʼ");
        }
        Iterator<Integer> it = sendDataHandles.keySet().iterator();
        int key;
        while (it.hasNext()) {
            key = it.next();
            SendFuture sf = sendDataHandles.get(key);
            if (sf != null && isTimeOut(sf)) {
                sendDataHandles.remove(key);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("������ڵ�sendFuture���� ��" + sf.getMessageId() + " ����ʱ��: "
                                 + sf.getSendTime());
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + " ���Ƚ���");
        }
    }

    /**
     * @param sendFuture
     *
     * @return
     * ��ʱ�Ǹ���SendFuture ���󴴽�ʱ���жϡ�
     */
    private boolean isTimeOut(SendFuture sendFuture) {
        if ((sendFuture.getSendTime() + sendFuture.getTimeOut()) < System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public Map<Integer, SendFuture> getSendDataHandles() {
        return sendDataHandles;
    }

    public void setSendDataHandles(Map<Integer, SendFuture> sendDataHandles) {
        this.sendDataHandles = sendDataHandles;
    }

}
