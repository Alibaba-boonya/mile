/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ExceptionEvent;

import com.alipay.mile.communication.MessageListener;
import com.alipay.mile.communication.ServerRef;
import com.alipay.mile.message.ClientConnectMessage;
import com.alipay.mile.message.ClientReConnectMessage;
import com.alipay.mile.message.CommonErrorMessage;
import com.alipay.mile.message.CommonOkMessage;
import com.alipay.mile.message.DocStartCommandMessage;
import com.alipay.mile.message.DocStopCommandMessage;
import com.alipay.mile.message.MergeStartCommandMessage;
import com.alipay.mile.message.MergeStartLeadQueryMessage;
import com.alipay.mile.message.MergeStopCommandMessage;
import com.alipay.mile.message.MergeStopLeadQueryMessage;
import com.alipay.mile.message.Message;
import com.alipay.mile.message.MessageFactory;
import com.alipay.mile.message.SqlExectueErrorMessage;
import com.alipay.mile.message.SqlExecuteMessage;
import com.alipay.mile.message.SqlPreExecuteMessage;
import com.alipay.mile.message.m2d.SpecifyQueryExecuteMessage;

/**
 * ͨ����Ϣ����������
 *
 * @author jin.qian
 * @version $Id: MergeServerMessageListener.java, v 0.1 2011-5-10 ����01:37:26
 *          jin.qian Exp $
 */
public class MergeServerMessageListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(MergeServerMessageListener.class
                                           .getName());
    private final ProxyServer   server;

    public MergeServerMessageListener(ProxyServer server) {
        this.server = server;
    }

    /**
     * @see com.alipay.mile.communication.MessageListener#receiveMessage(byte[],
     *      org.jboss.netty.channel.Channel)
     */
    @Override
    public void receiveMessage(byte[] data, Channel channel, long messageStartTime)
                                                                                   throws IOException {
        long startTime = System.currentTimeMillis();
        Message request = MessageFactory.toMessage(data);
        Message response = null;
        if (request instanceof SqlPreExecuteMessage) {// SqlPreExecuteMessage
            SqlPreExecuteMessage requestMessage = (SqlPreExecuteMessage) request;
            if ((startTime - messageStartTime) >= requestMessage.getExeTimeout()) {
                LOGGER.error("����sql��Ϣ�ڶ�����ȴ���ʱ --MessageID: " + requestMessage.getId());
                SqlExectueErrorMessage resMessage = new SqlExectueErrorMessage();
                resMessage.setId(requestMessage.getId());
                resMessage.setVersion(requestMessage.getVersion());
                resMessage.setErrDescription("����sql��Ϣ�ڶ�����ȴ���ʱ--MessageID: "
                                             + requestMessage.getId());
                response = resMessage;
            } else if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                // ����Ԥ������Ϣ
                requestMessage
                    .setExeTimeout((int) (requestMessage.getExeTimeout() - (startTime - messageStartTime)));
                response = server.processSqlPreExecuteMessage(requestMessage, channel
                    .getRemoteAddress().toString());
            }
        } else if (request instanceof SqlExecuteMessage) {// SqlExecuteMessage
            SqlExecuteMessage requestMessage = (SqlExecuteMessage) request;
            if ((startTime - messageStartTime) >= requestMessage.getExeTimeout()) {
                LOGGER.error("����sql��Ϣ�ڶ�����ȴ���ʱ --MessageID: " + requestMessage.getId());
                SqlExectueErrorMessage resMessage = new SqlExectueErrorMessage();
                resMessage.setId(requestMessage.getId());
                resMessage.setVersion(requestMessage.getVersion());
                resMessage.setErrDescription("����sql��Ϣ�ڶ�����ȴ���ʱ--MessageID: "
                                             + requestMessage.getId());
                response = resMessage;

            } else if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                // ����sql ��Ϣ
                requestMessage
                    .setExeTimeout((int) (requestMessage.getExeTimeout() - (startTime - messageStartTime)));
                response = server.processSqlExecuteMessage(requestMessage, channel
                    .getRemoteAddress().toString());
            }
        } else if (request instanceof SpecifyQueryExecuteMessage) {// SqlExecuteMessage
            if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                // ����sql ��Ϣ
                response = server
                    .processSpecifyQueryExecuteMessage((SpecifyQueryExecuteMessage) request);
            }
        } else if (request instanceof ClientConnectMessage) {// ClientConnectMessage
            // ����������Ϣ
            if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                response = server.processClientConnectMessage((ClientConnectMessage) request);
            }
        } else if (request instanceof ClientReConnectMessage) {// ClientReConnectMessage
            // ����������Ϣ
            if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                response = server.processClientReConnectMessage((ClientReConnectMessage) request);
            }
        } else if (request instanceof MergeStartCommandMessage) {// MergeStartCommandMessage
            // �����ֶ�������Ϣ
            server.getServer().setOnline(true);
            CommonOkMessage commonOkMessage = new CommonOkMessage();
            commonOkMessage.setOkDescription("mergerServer �����ֶ�����״̬");
            response = commonOkMessage;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("mergerServer �����ֶ�����״̬");
            }
        } else if (request instanceof MergeStopCommandMessage) {// MergeStopCommandMessage
            // �����ֶ�������Ϣ
            server.getServer().setOnline(false);
            CommonOkMessage commonOkMessage = new CommonOkMessage();
            commonOkMessage.setOkDescription("mergerServer �����ֶ�����״̬");
            response = commonOkMessage;
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("mergerServer �����ֶ�����״̬ ");
            }
        } else if (request instanceof DocStartCommandMessage) {// DocStartCommandMessage
            // ����������Ϣ
            DocStartCommandMessage dsc = (DocStartCommandMessage) request;
            ServerRef sr;
            Iterator<ServerRef> it = server.getClient().getServerRefFail().iterator();
            while (it.hasNext()) {
                sr = it.next();
                if ((sr.getServerIp() + ":" + sr.getPort()).equals(dsc.getDocServerIp())) {
                    sr.setOnline(true);
                    CommonOkMessage commonOkMessage = new CommonOkMessage();
                    commonOkMessage.setOkDescription("DocServer " + dsc.getDocServerIp()
                                                     + " �����ֶ�����״̬");
                    response = commonOkMessage;
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("DocServer " + dsc.getDocServerIp() + " �����ֶ�����״̬");
                    }
                    break;
                }
            }
            if (response == null) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("û���ҵ����ֶ����ߵ�DocServer "
                                                       + dsc.getDocServerIp());
                response = commonErrorMessage;
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("û���ҵ����ֶ����ߵ�DocServe " + dsc.getDocServerIp());
                }
            }
        } else if (request instanceof DocStopCommandMessage) {// DocStopCommandMessage
            // ����Doc������Ϣ
            DocStopCommandMessage dsc = (DocStopCommandMessage) request;
            Iterator<ServerRef> it = server.getClient().getServerRefOk().iterator();
            ServerRef sr;
            while (it.hasNext()) {
                sr = it.next();
                if ((sr.getServerIp() + ":" + sr.getPort()).equals(dsc.getDocServerIp())) {
                    sr.setOnline(false);
                    if (server.getClient().getServerRefOk().remove(sr)) {
                        server.getClient().getServerRefFail().add(sr);
                    }
                    break;
                }
            }
            it = server.getClient().getServerRefFail().iterator();
            while (it.hasNext()) {
                sr = it.next();
                if ((sr.getServerIp() + ":" + sr.getPort()).equals(dsc.getDocServerIp())) {
                    sr.setOnline(false);
                    CommonOkMessage commonOkMessage = new CommonOkMessage();
                    commonOkMessage.setOkDescription("DocServer " + dsc.getDocServerIp()
                                                     + " �����ֶ�����״̬");
                    response = commonOkMessage;
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("DocServer " + dsc.getDocServerIp() + " �����ֶ�����״̬ ");
                    }
                    break;
                }
            }
            if (response == null) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("û���ҵ����ֶ����ߵ�DocServer "
                                                       + dsc.getDocServerIp());
                response = commonErrorMessage;
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("û���ҵ����ֶ����ߵ�DocServer " + dsc.getDocServerIp());
                }
            }
        } else if (request instanceof MergeStartLeadQueryMessage) {
            if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                response = server.processStartLeadQueryMessage((MergeStartLeadQueryMessage) request);
            }
            
        } else if (request instanceof MergeStopLeadQueryMessage) {
            if (!server.getServer().isOnline()) {
                CommonErrorMessage commonErrorMessage = new CommonErrorMessage();
                commonErrorMessage.setErrorDescription("mergerServer �����ֶ�����״̬");
                commonErrorMessage.getErrorParameters().add(channel.getLocalAddress().toString());
                response = commonErrorMessage;
            } else {
                response = server.processStopLeadQueryMessage((MergeStopLeadQueryMessage) request);
            }
        }

        // return
        assert response != null : "Process request message NO response message";
        response.setId(request.getId());
        // ���ؽ��
        channel.write(MessageFactory.toSendMessage(response));
    }

    /**
     * @see com.alipay.mile.communication.MessageListener#handleException(org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void handleException(ExceptionEvent e) {
        LOGGER.error("handelException" + e.getCause());
    }

}
