/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.alipay.mile.log.DigestLogUtil;
import com.alipay.mile.util.SimpleThreadFactory;

/**
 * @author jin.qian
 * @version $Id: MessageManager.java,v 0.1 2011-4-6 ����10:51:34 jin.qian Exp $
 * listener �����࣬
 * exec ���첽���� ��Ϣ����
 */
public class MessageManager {

    private static final Logger   DIGESTLOGGER = Logger.getLogger("COMMON-DIGEST");

    /** ��Ϣlistenters ��ע������ */
    private List<MessageListener> messagelisteners;

    /**��Ϣ�����̳߳� ������������� ���߳���cpu����  */
    public final ThreadPoolExecutor  exec;
    public final ThreadPoolExecutor  queryExec;

    
    private class QueueDigestPrint implements Runnable{
        @Override
        public void run() {
            if(DIGESTLOGGER.isInfoEnabled()){
                DIGESTLOGGER.info("��ǰ������г���:" + exec.getQueue().size());
                DIGESTLOGGER.info("��ǰ��ѯ���г���:" + queryExec.getQueue().size());
            }
        }
    }
    
    public MessageManager(int execMin, int execMax, int queryExecMin, int queryExecMax,
                          int keepAliveTime, int blockingQueueCount) {
        exec = new ThreadPoolExecutor(execMin, execMax, keepAliveTime, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(blockingQueueCount), new SimpleThreadFactory(
                "MessageManager-exec-core", false), new ThreadPoolExecutor.CallerRunsPolicy());
        queryExec = new ThreadPoolExecutor(queryExecMin, queryExecMax, keepAliveTime,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(blockingQueueCount),
            new SimpleThreadFactory("MessageManager-queryexec-core", false),
            new ThreadPoolExecutor.CallerRunsPolicy());
        messagelisteners = new CopyOnWriteArrayList<MessageListener>();
        
        // ע��ժҪ��־��ӡ����, ÿ��60s��ӡһ��
        DigestLogUtil.registDigestTask(new QueueDigestPrint(), 30, 60, TimeUnit.SECONDS);
    }

    /**
     * ע��listener
     * @param MessageListener ������Ϣ��listener
     */
    public void addMessageListener(MessageListener listener) {
        messagelisteners.add(listener);
    }

    /**
     * ����MessageListeners �б�
     * @return List<MessageListener>
     */
    public List<MessageListener> getMessagelisteners() {
        return messagelisteners;
    }

    /**
     * ɾ��Listener
     * @param MessageListener
     */
    public void rmoveMessageListener(MessageListener listener) {
        messagelisteners.remove(listener);
    }

    /**
     * ����Messagelisteners
     * @param messagelisteners
     */
    public void setMessagelisteners(List<MessageListener> messagelisteners) {
        this.messagelisteners = messagelisteners;
    }

}
