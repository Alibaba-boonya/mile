/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.alipay.mile.Config;
import com.alipay.mile.Constants;
import com.alipay.mile.communication.MileClient;
import com.alipay.mile.communication.SendFuture;
import com.alipay.mile.communication.ServerRef;
import com.alipay.mile.message.AccessStateRsMessage;
import com.alipay.mile.message.Message;
import com.alipay.mile.message.m2d.AccessStateMessage;

/**
 * @author jin.qian
 * @version $Id: ClientMergeServicesTimerTask.java,v 0.1 2011-4-6 ����05:49:52
 *          jin.qian Exp $
 */
public class CheckDocServersTimerTask implements Runnable {

    private static final Logger   LOGGER = Logger.getLogger(CheckDocServersTimerTask.class);

    private final List<ServerRef> serverRefOk;
    private final List<ServerRef> serverRefFail;
    private final MileClient      engineConnector;

    public CheckDocServersTimerTask(MileClient engineConnector) {
        this.serverRefOk = engineConnector.getServerRefOk();
        this.serverRefFail = engineConnector.getServerRefFail();
        this.engineConnector = engineConnector;
    }

    /**
     * ��docserver����״̬��ȡ���ģ���ȡdocserver��״̬
     * @param channel	��docserver���ͱ��ĵ�channel
     * @return			��ΧֵΪtrueʱ����docserver���ã�Ϊfalseʱ����docserver������
     */
    public static boolean getDocServerState(Channel channel, MileClient mileClient) {
        //������عر�ֱ�ӷ���true
        if (!Config.getAllowMergerServerCheckDocServer()) {
            return true;
        }
        AccessStateMessage accessStateMessage = new AccessStateMessage();
        List<String> states = new ArrayList<String>();
        states.add(Constants.DOCSERVER_STATE_READABLE);
        accessStateMessage.setStates(states);
        boolean state = false;

        try {
            SendFuture sendFuture = mileClient.futureSendData(channel, accessStateMessage,
                Constants.GET_STATE_TIME_OUT, true);
            Message message = sendFuture.get();
            if (message instanceof AccessStateRsMessage) {
                AccessStateRsMessage stateRsMessage = (AccessStateRsMessage) message;
                byte readable = (Byte) stateRsMessage.getStates().get(
                    Constants.DOCSERVER_STATE_READABLE);
                if (readable == 1) {
                    state = true;
                }
            } else {
                LOGGER.error("��docserver��ѯ״̬ʱ, docserver���ش������ݰ� " + message);
            }
        } catch (IOException e) {
            LOGGER.error(e);
        } catch (InterruptedException e) {
            LOGGER.error(e);
        } catch (ExecutionException e) {
            LOGGER.error(e);
        }

        return state;
    }

    /**
     * ��ʱ��������״̬
     *
     */
    @Override
    public void run() {
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("��ʱ��������״̬ ��ʼ");
        }
        Iterator<ServerRef> itok = serverRefOk.iterator();
        ServerRef ms;
        // ���������ӿ���״̬
        while (itok.hasNext()) {
            ms = itok.next();
            if (ms.getChannel() == null || !ms.getChannel().isConnected() || !ms.isAvailable()
                || !ms.isOnline() || !getDocServerState(ms.getChannel(), engineConnector)) {
                ms.setAvailable(false);
                if (serverRefOk.remove(ms)) {
                    serverRefFail.add(ms);
                }
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("��⵽ʧ������ת�Ƶ�ʧ��DocServer�б� ->" + ms.getServerIp() + ":"
                                + ms.getPort());
                }
            }
        }
        // ���ǿ������ӵĿ���״̬���������ӷǿ�������ʹ���ɿ��ã�ά�����������б�
        Iterator<ServerRef> itfail = serverRefFail.iterator();
        while (itfail.hasNext()) {
            ms = itfail.next();
            if ((ms.getChannel() == null || !ms.getChannel().isConnected()) && ms.isOnline()) {
                ms.setAvailable(false);
                Channel channel = engineConnector.getConnectedChannel(ms.getServerIp(), ms
                    .getPort());
                if (channel.isConnected() && getDocServerState(channel, engineConnector)) {
                    ms.setAvailable(true);
                    ms.setChannel(channel);
                    if (serverRefFail.remove(ms)) {
                        serverRefOk.add(ms);
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("�ɹ�����DocServer ->" + ms.getChannel().getLocalAddress() + "->"
                                    + ms.getChannel().getRemoteAddress());
                    }
                } else {
                    ms.setAvailable(false);
                }
            }
            else if (ms.getChannel() != null && ms.getChannel().isConnected() && ms.isOnline()
                       && getDocServerState(ms.getChannel(), engineConnector)) {
                ms.setAvailable(true);
                if (serverRefFail.remove(ms)) {
                    serverRefOk.add(ms);
                }
            }
        }
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("��ʱ��������״̬ ����");
        }
    }

}
