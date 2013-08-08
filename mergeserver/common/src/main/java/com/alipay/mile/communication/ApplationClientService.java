/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.alipay.mile.message.ClientConnRespError;
import com.alipay.mile.message.ClientConnRespOKMessage;
import com.alipay.mile.message.ClientConnectMessage;
import com.alipay.mile.message.ClientReConnectMessage;
import com.alipay.mile.message.CommonErrorMessage;
import com.alipay.mile.message.Message;
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * @author jin.qian
 * @version $Id: ApplationClientService.java,v 0.1 2011-4-6 ����05:48:00 jin.qian
 *          Exp $
 */
public class ApplationClientService {

    private static final Logger LOGGER = Logger.getLogger(ApplationClientService.class.getName());

    /**
     * @param mergeServerList
     * @param mileClient
     * @param clientConnectMessage
     *            ����ʱ����server�б�������
     */
    public static void initMergeServers(List<String> mergeServerList, MileClient mileClient,
                                        ClientConnectMessage clientConnectMessage) {
        if (null == mergeServerList || mergeServerList.size() == 0) {
            return;
        }
        List<ServerRef> mergerOk = new CopyOnWriteArrayList<ServerRef>();
        List<ServerRef> mergerFail = new CopyOnWriteArrayList<ServerRef>();
        for (String hostUrl : mergeServerList) {
            String[] hostTemp = hostUrl.split(":");
            Channel channel = null;
            if (hostTemp.length == 2) {
                // ��������
                channel = mileClient
                    .getConnectedChannel(hostTemp[0], Integer.parseInt(hostTemp[1]));
                // �������������
                ServerRef ms = new ServerRef();
                ms.setChannel(channel);
                ms.setPort(Integer.parseInt(hostTemp[1]));
                ms.setServerIp(hostTemp[0]);
                ms.setClientUserName(clientConnectMessage.getUserName());
                ms.setClientPassWord(clientConnectMessage.getPassWord());
                ms.setClientProperty(clientConnectMessage.getClientProperty());
                ms.setVersion(clientConnectMessage.getVersion());

                if (channel != null && channel.isConnected()) {
                    // ����������Ϣ
                    Message message = clientConnet(clientConnectMessage, ms, mileClient);
                    // ������֤�ɹ�
                    if (message instanceof ClientConnRespOKMessage) {
                        ClientConnRespOKMessage reslutMessage = (ClientConnRespOKMessage) message;
                        ms.setAvailable(true);
                        ms.setOnline(true);
                        ms.setSessionId(reslutMessage.getSessionID());
                        ms.setServerDescription(reslutMessage.getServerDescription());
                        ms.setServerproperties(reslutMessage.getServerproperties());
                        mergerOk.add(ms);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("�ɹ�����MergerServer ->" + channel.getLocalAddress() + "->"
                                        + channel.getRemoteAddress());
                        }
                    } else if (message instanceof ClientConnRespError) {
                        // ������֤ʧ��
                        ClientConnRespError reslutMessage = (ClientConnRespError) message;
                        ms.setAvailable(false);
                        ms.setConnErrCode(reslutMessage.getConnErrCode());
                        ms.setErrParameter(reslutMessage.getErrParameter());
                        ms.setErrDescription(reslutMessage.getErrDescription());
                        mergerFail.add(ms);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("ʧ������MergerServer ->" + channel.getLocalAddress() + "->"
                                        + channel.getRemoteAddress());
                        }
                    } else if (message instanceof CommonErrorMessage) {
                        // merger�����ֶ�����
                        ms.setAvailable(false);
                        ms.setOnline(false);
                        mergerFail.add(ms);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("MergerServer�����ֶ����� ->" + channel.getLocalAddress() + "->"
                                        + channel.getRemoteAddress());
                        }
                    } else if (message == null) {
                        ms.setAvailable(false);
                        mergerFail.add(ms);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("ʧ�ܵ�Session��֤����MergerServer ->" + channel.getLocalAddress()
                                        + "->" + channel.getRemoteAddress());
                        }
                    }
                } else {
                    ms.setAvailable(false);
                    mergerFail.add(ms);
                    if(LOGGER.isInfoEnabled()){
                        LOGGER.info("ʧ�ܵ�Session��֤����MergerServer");
                    }
                }
            }

        }//for end
        mileClient.getServerRefOk().clear();
        mileClient.getServerRefOk().addAll(mergerOk);
        mileClient.getServerRefFail().clear();
        mileClient.getServerRefFail().addAll(mergerFail);
    }

    /**
     * @param message
     * @param mergeServerInfo
     * @param mileClient
     * @return ���� ���ӽ�����Ϣ
     */
    public static Message clientConnet(ClientConnectMessage message, ServerRef mergeServerInfo,
                                       MileClient mileClient) {
        try {
            SendFuture sendFuture = mileClient.futureSendData(mergeServerInfo.getChannel(),
                message, 10000, true);
            return sendFuture.get();
        } catch (Exception e) {
            LOGGER.warn("����ʧ��", e);
        }
        return null;

    }

    /**
     * @param message
     * @param mergeServerInfo
     * @param mileClient
     * @return ��������������Ϣ
     */
    public static Message clientReConnet(ClientReConnectMessage message, ServerRef mergeServerInfo,
                                         MileClient mileClient) {
        try {
            SendFuture sendFuture = mileClient.futureSendData(mergeServerInfo.getChannel(),
                message, 10000, true);
            return sendFuture.get();
        } catch (Exception e) {
            LOGGER.warn("��������������Ϣ", e);
        }
        return null;
    }

    /**
     * @param mergeServersOk
     * @param mergeServersFail
     * @param mileClient
     * @return �����÷�����
     * @throws SqlExecuteException
     */
    public static ServerRef getRodemMergeServer(MileClient mileClient) throws SqlExecuteException {

        if (mileClient.getServerRefOk().isEmpty()) {
            StringBuffer sb = new StringBuffer(64);
            sb.append("û�п��õ�MergeServer. MergeServerList:");
            sb.append(mileClient.getServerRefOk().size());
            LOGGER.warn(sb.toString());
            // throw new NullPointerException("û�п��õ�MergeServerOK");
            return null;
        }

        ServerRef mi = getMergeSI(mileClient.getServerRefOk());

        if (mi == null) {
            checkMergeServer(mileClient);
            mi = getMergeSI(mileClient.getServerRefOk());
        }
        if (mi == null) {
            LOGGER.warn("�������Ժ�û�п��õ�MergeServer");
            throw new SqlExecuteException("�������Ժ�û�п��õ�MergeServer");
        }
        return mi;
    }

    /**
     * @param mergeServers
     * @return
     */
    private static ServerRef getMergeSI(List<ServerRef> mergeServers) {
        ServerRef mi = null;
        int index = (int) (Math.random() * mergeServers.size());

        mi = mergeServers.get(index);
        if (mi.isAvailable() && mi.getChannel().isConnected()) {
            return mi;
        }
        LOGGER.warn("mergeServers.size()" + mergeServers.size());
        LOGGER.warn("mergeServers.size()" + mergeServers.size());
        LOGGER.warn("û�п��õ�MergeServer");
        return null;
    }

    /**
     * @param mergeServersOk
     * @param mergeServersFail
     * @param mileClient
     *            ά��������״̬
     */
    public static void checkMergeServer(MileClient mileClient) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + ": checkMergeServer is run");
        }
        Iterator<ServerRef> itok = mileClient.getServerRefOk().iterator();
        ServerRef ms;
        while (itok.hasNext()) {
            ms = itok.next();
            if (ms.getChannel() == null || !ms.getChannel().isConnected() || !ms.isAvailable()
                || !ms.isOnline()) {
                ms.setAvailable(false);
                if (mileClient.getServerRefOk().remove(ms)) {
                    mileClient.getServerRefFail().add(ms);
                }
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("����MergerServerʧ�� ->" + ms.getChannel().getLocalAddress() + "->"
                                + ms.getChannel().getRemoteAddress());
                }
            }
        }
        Iterator<ServerRef> itfail = mileClient.getServerRefFail().iterator();
        while (itfail.hasNext()) {
            ms = itfail.next();
            if (ms.getChannel() == null || !ms.getChannel().isConnected()) {
                ms.setAvailable(false);
                Channel channel = mileClient.getConnectedChannel(ms.getServerIp(), ms.getPort());
                ms.setChannel(channel);
            }
            // ����������Ϣ
            if (ms.getChannel().isConnected() && !ms.isAvailable()) {
                if (ms.getSessionId() != 0) {
                    // ����������Ϣ
                    ClientReConnectMessage clientReConnectMessage = new ClientReConnectMessage();
                    clientReConnectMessage.setClientProperty(ms.getClientProperty());
                    clientReConnectMessage.setPassWord(ms.getClientPassWord());
                    clientReConnectMessage.setUserName(ms.getClientUserName());
                    clientReConnectMessage.setSessionID(ms.getSessionId());
                    clientReConnectMessage.setVersion(ms.getVersion());
                    Message message = ApplationClientService.clientReConnet(clientReConnectMessage,
                        ms, mileClient);
                    // �����������
                    if (message instanceof ClientConnRespOKMessage) {
                        ClientConnRespOKMessage reslutMessage = (ClientConnRespOKMessage) message;
                        ms.setAvailable(true);
                        ms.setOnline(true);
                        ms.setSessionId(reslutMessage.getSessionID());
                        ms.setServerDescription(reslutMessage.getServerDescription());
                        ms.setServerproperties(reslutMessage.getServerproperties());
                        if (mileClient.getServerRefFail().remove(ms)) {
                            mileClient.getServerRefOk().add(ms);
                        }
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("�ɹ�����MergerServer ->" + ms.getChannel().getLocalAddress()
                                        + "->" + ms.getChannel().getRemoteAddress());
                        }
                    } else if (message instanceof ClientConnRespError) {
                        // ��������ʧ�ܽ��
                        ClientConnRespError reslutMessage = (ClientConnRespError) message;
                        ms.setAvailable(false);
                        ms.setConnErrCode(reslutMessage.getConnErrCode());
                        ms.setErrParameter(reslutMessage.getErrParameter());
                        ms.setErrDescription(reslutMessage.getErrDescription());
                    } else if (message instanceof CommonErrorMessage) {
                        // merger�����ֶ�����
                        ms.setAvailable(false);
                        ms.setOnline(false);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("MergerServer�����ֶ����� ->" + ms.getChannel().getLocalAddress()
                                        + "->" + ms.getChannel().getRemoteAddress());
                        }
                    } else if (LOGGER.isInfoEnabled() && message == null) {
                        LOGGER.info("ʧ�ܵ�Session��֤����MergerServer ->"
                                    + ms.getChannel().getLocalAddress() + "->"
                                    + ms.getChannel().getRemoteAddress());
                    }
                } else {
                    // ����������Ϣ
                    ClientConnectMessage clientConnectMessage = new ClientConnectMessage();
                    clientConnectMessage.setClientProperty(ms.getClientProperty());
                    clientConnectMessage.setPassWord(ms.getClientPassWord());
                    clientConnectMessage.setUserName(ms.getClientUserName());
                    clientConnectMessage.setVersion(ms.getVersion());
                    Message message = ApplationClientService.clientConnet(clientConnectMessage, ms,
                        mileClient);
                    // �����ӽ��
                    if (message instanceof ClientConnRespOKMessage) {
                        ClientConnRespOKMessage reslutMessage = (ClientConnRespOKMessage) message;
                        ms.setAvailable(true);
                        ms.setSessionId(reslutMessage.getSessionID());
                        ms.setServerDescription(reslutMessage.getServerDescription());
                        ms.setServerproperties(reslutMessage.getServerproperties());
                        mileClient.getServerRefFail().remove(ms);
                        mileClient.getServerRefOk().add(ms);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("�ɹ�����MergerServer ->" + ms.getChannel().getLocalAddress()
                                        + "->" + ms.getChannel().getRemoteAddress());
                        }
                    } else if (message instanceof ClientConnRespError) {
                        // ��������ʧ�ܽ��
                        ClientConnRespError reslutMessage = (ClientConnRespError) message;
                        ms.setAvailable(false);
                        ms.setConnErrCode(reslutMessage.getConnErrCode());
                        ms.setErrParameter(reslutMessage.getErrParameter());
                        ms.setErrDescription(reslutMessage.getErrDescription());
                    } else if (message instanceof CommonErrorMessage) {
                        // merger�����ֶ�����
                        ms.setAvailable(false);
                        ms.setOnline(false);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("MergerServer�����ֶ����� ->" + ms.getChannel().getLocalAddress()
                                        + "->" + ms.getChannel().getRemoteAddress());
                        }
                    } else if (LOGGER.isInfoEnabled() && message == null) {
                        LOGGER.info("ʧ�ܵ�Session��֤����MergerServer ->"
                                    + ms.getChannel().getLocalAddress() + "->"
                                    + ms.getChannel().getRemoteAddress());
                    }
                }
            }
            if (!ms.getChannel().isConnected()) {
                ms.setAvailable(false);
                LOGGER.warn("�޸�ʧ������ʧ��" + ms.getServerIp() + ":" + ms.getPort());
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(Thread.currentThread().getName() + ": checkMergeServer is end");
        }
    }
}
