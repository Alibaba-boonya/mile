/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.alipay.mile.message.AbstractMessage;
import com.alipay.mile.message.Message;
import com.alipay.mile.message.MessageFactory;
import com.alipay.mile.util.SimpleThreadFactory;

/**
 * @author jin.qian, yuzhong.zhao
 * @version $Id: MileClient.java,v 0.1 2011-4-6 ����11:10:32 jin.qian Exp $
 */
public class MileClient {

    private static final Logger          LOGGER              = Logger.getLogger(MileClient.class
                                                                 .getName());
    /**ͨѶ����������  */
    private ClientBootstrap              bootstrap;

    /**���������̳߳�  */
    private Executor                     bossExecutor;
    /**ͨѶ�����̳߳�*/
    private Executor                     workerExecutor;
    /**��������  */
    private MessageClientPipelineFactory messageClientPipelineFactory;
    /**���������߳���  */
    private int                          bossExecutorCount   = 0;
    /**ͨѶ��������  */
    private int                          workerExecutorCount = 0;
    /** �ڵ�ţ���Node��mapӳ��*/
    private ConcurrentMap<Integer, Node> nodes               = new ConcurrentHashMap<Integer, Node>();
    /**���÷������б�  */
    private List<ServerRef>              serverRefOk         = new CopyOnWriteArrayList<ServerRef>();
    /**�ǿ��÷������б�  */
    private List<ServerRef>              serverRefFail       = new CopyOnWriteArrayList<ServerRef>();

    /**SendFuture����*/
    private Map<Integer, SendFuture>     sendDataHandles     = new ConcurrentHashMap<Integer, SendFuture>();
    /**��Ϣid������  */
    private AtomicInteger                sendDataHandleID    = new AtomicInteger();
    /**���sendeFuture��ʱ������  */
    public ScheduledExecutorService      timer               = Executors
                                                                 .newScheduledThreadPool(
                                                                     1,
                                                                     new SimpleThreadFactory(
                                                                         "MileClient_SendFutureCleanThreadGroup",
                                                                         false));

    /**
     * ����Clent ������Ĭ�ϲ���
     */
    public void customBootstrap() {
        //bossExecutor �߳� Ĭ��Ϊ cpu*2
        if (bossExecutorCount == 0) {
            bossExecutorCount = Runtime.getRuntime().availableProcessors() * 2;
        }
        //workerExecutor �߳� Ĭ��Ϊ cpu*2
        if (workerExecutorCount == 0) {
            workerExecutorCount = Runtime.getRuntime().availableProcessors() * 2;
        }
        //���������̳߳�
        if (bossExecutor == null) {
            //            bossExecutor = Executors.newFixedThreadPool(bossExecutorCount);
            bossExecutor = Executors.newCachedThreadPool(new SimpleThreadFactory(
                "MileClient-bossExecutor-core", false));
        }
        //������Ϣ�̳߳�
        if (workerExecutor == null) {
            //            workerExecutor = Executors.newFixedThreadPool(workerExecutorCount);
            workerExecutor = Executors.newFixedThreadPool(workerExecutorCount,
                new SimpleThreadFactory("MileClient-workerExecutor-core", false));
        }
        //�������� ��ʼ��
        if (getMessageClientPipelineFactory() == null) {
            messageClientPipelineFactory = new MessageClientPipelineFactory(serverRefOk,
                serverRefFail, sendDataHandles);
        }
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossExecutor,
            workerExecutor, workerExecutorCount);
        //��ʼ�� bootstrap
        bootstrap = new ClientBootstrap(channelFactory);
        bootstrap.setPipelineFactory(getMessageClientPipelineFactory());
        bootstrap.setOption("child.tcpNoDelay", true);
        //����tpcip������ �������ܿ���
        bootstrap.setOption("child.keepAlive", true);
        //����SendFuture������
        timer.scheduleAtFixedRate(new SendFutureCleanTimerTask(sendDataHandles), 1, 60,
            TimeUnit.SECONDS);
    }


    /**
     * �ر�client���Ͽ�����
     */
    public void close() {
        timer.shutdown();
        for (ServerRef sr : serverRefOk) {
            sr.getChannel().disconnect().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
        for (ServerRef sr : serverRefFail) {
            sr.getChannel().disconnect().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
        
        bootstrap.getFactory().releaseExternalResources();
    }

    
    
    
    /**
     * Future ģʽ �������� ��������ʱ�������̣߳�Ӧ���߳�getʱ����Ӧ���̡߳�
     * @param channel ͨ��channle
     * @param message �����͵���Ϣ
     * @param TimeOut ���ͳ�ʱ
     * @param autoMessageId �Զ�������ϢId �����Ϣid�ǲ��ҷ���ʱ�ĵ��ö���
     * @return SendFuture ����
     * @throws IOException
     */
    public SendFuture futureSendData(Channel channel, Message message, int TimeOut,
                                     boolean autoMessageId) throws IOException {
        if (autoMessageId && message instanceof AbstractMessage) {
            AbstractMessage tempMag = (AbstractMessage) message;
            tempMag.setId(sendDataHandleID.incrementAndGet());
        }
        SendFuture task = new SendFuture(message.getId(), channel, TimeOut);
        task.send(MessageFactory.toSendMessage(message));
        return task;
    }

    /**
     * @param channel
     * @param message
     * @param autoMessageId
     * @return
     * @throws IOException
     * ���������� ��Ϣ���޷���ֵ
     */
    public boolean sendData(Channel channel, Message message, boolean autoMessageId)
                                                                                    throws IOException {
        //����״̬��־
        boolean flag = false;
        if (autoMessageId && message instanceof AbstractMessage) {
            AbstractMessage tempMag = (AbstractMessage) message;
            //��Ϣid��ֵ
            tempMag.setId(sendDataHandleID.incrementAndGet());
        }
        if (channel.isConnected()) {
            //����Ϣת����byte[]���͡����ط���״̬
            flag = channel.write(MessageFactory.toSendMessage(message)).awaitUninterruptibly()
                .isSuccess();
        }
        return flag;
    }

    /**
     * @param host ������ַ
     * @param port  �˿ں�
     * @return ������������ͨѶchannle
     * @throws Throwable
     * �õ�һ��ͨ��channel
     */
    public Channel getConnectedChannel(String host, int port) {
        if (bootstrap == null) {
            customBootstrap();
        }
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        Channel channel = future.awaitUninterruptibly().getChannel();
        if (future.getCause() != null) {
            LOGGER.warn("����ʧ��->" + host + ":" + port);
            //            throw future.getCause();
        }
        return channel;
    }

    /**
     * @return
     */
    public ClientBootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * @param bootstrap
     */
    public void setBootstrap(ClientBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    /**
     * @return
     */
    public Executor getBossExecutor() {
        return bossExecutor;
    }

    /**
     * @param bossExecutor
     */
    public void setBossExecutor(Executor bossExecutor) {
        this.bossExecutor = bossExecutor;
    }

    /**
     * @return
     */
    public Executor getWorkerExecutor() {
        return workerExecutor;
    }

    /**
     * @param workerExecutor
     */
    public void setWorkerExecutor(Executor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    /**
     * @param messageClientPipelineFactory
     */
    public void setMessageClientPipelineFactory(
                                                MessageClientPipelineFactory messageClientPipelineFactory) {
        this.messageClientPipelineFactory = messageClientPipelineFactory;
    }

    /**
     * @return
     */
    public MessageClientPipelineFactory getMessageClientPipelineFactory() {
        return messageClientPipelineFactory;
    }

    /**
     * @return
     */
    public List<ServerRef> getServerRefOk() {
        return serverRefOk;
    }

    /**
     * @param mergeServersOk
     */
    public void setServerRefOk(List<ServerRef> mergeServersOk) {
        this.serverRefOk = mergeServersOk;
    }

    /**
     * @return
     */
    public List<ServerRef> getServerRefFail() {
        return serverRefFail;
    }

    /**
     * @param mergeServersFail
     */
    public void setServerRefFail(List<ServerRef> mergeServersFail) {
        this.serverRefFail = mergeServersFail;
    }

    /**
     * @return
     */
    public int getBossExecutorCount() {
        return bossExecutorCount;
    }

    /**
     * @param bossExecutorCount
     */
    public void setBossExecutorCount(int bossExecutorCount) {
        this.bossExecutorCount = bossExecutorCount;
    }

    /**
     * @return
     */
    public int getWorkerExecutorCount() {
        return workerExecutorCount;
    }

    /**
     * @param workerExecutorCount
     */
    public void setWorkerExecutorCount(int workerExecutorCount) {
        this.workerExecutorCount = workerExecutorCount;
    }

    public Map<Integer, SendFuture> getSendDataHandles() {
        return sendDataHandles;
    }

    public void setSendDataHandles(Map<Integer, SendFuture> sendDataHandles) {
        this.sendDataHandles = sendDataHandles;
    }

    public AtomicInteger getSendDataHandleID() {
        return sendDataHandleID;
    }

    public void setSendDataHandleID(AtomicInteger sendDataHandleID) {
        this.sendDataHandleID = sendDataHandleID;
    }

    public ConcurrentMap<Integer, Node> getNodes() {
        return nodes;
    }

    public void setNodes(ConcurrentMap<Integer, Node> nodes) {
        this.nodes = nodes;
    }
}
