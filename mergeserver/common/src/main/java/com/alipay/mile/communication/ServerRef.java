/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;

import com.alipay.mile.Config;
import com.alipay.mile.Constants;
import com.alipay.mile.message.KeyValueData;

/**
 * @author jin.qian
 * @version $Id: MergeServerInfo.java,v 0.1 2011-4-6 ����05:50:09 jin.qian Exp $
 *          ������״̬ BO
 */
public class ServerRef {

    /** docserver����ݣ���master��slave֮�֣�Ĭ��ΪSLAVE */
    private int                identity       = Constants.SLAVE;
    /** ������id */
    private int                serverId;
    /** �������� */
    private String             serverName;
    /** ������IP */
    private String             serverIp;
    /** �������˿� */
    private int                port;
    /** ������״̬ */
    private int                status;                                  // 1�� ���� 3������ 4�������� 5��ͣ��
    /** �������󶨵�ͨѶchannel */
    private Channel            channel;
    /**Ŀǰͨ���еĲ�����Ϣ����  */
    private AtomicInteger      insertMsgCount = new AtomicInteger(0);
    /**Ŀǰͨ���еĲ�ѯ��Ϣ����  */
    private AtomicInteger      queryMsgCount  = new AtomicInteger(0);
    /** �û��� */
    private String             clientUserName;
    /** �û����� */
    private byte[]             clientPassWord;
    /** client ���� */
    private List<KeyValueData> clientProperty;
    /** ������ͨѶsessionid */
    private int                sessionId;
    /** ���������� */
    private List<KeyValueData> serverproperties;
    /** ���������� */
    private String             serverDescription;
    /** ���Ӵ������ */
    private short              connErrCode;
    /** ���Ӵ������ */
    private List<Object>       errParameter   = new ArrayList<Object>();
    /** �������� */
    private String             errDescription;
    /** �Ƿ���� */
    private boolean            available;
    /** �Ƿ���� */
    private boolean            online         = true;
    /** ͨ�Ű汾�� */
    private short              version;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public List<KeyValueData> getServerproperties() {
        return serverproperties;
    }

    public void setServerproperties(List<KeyValueData> serverproperties) {
        this.serverproperties = serverproperties;
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public String getClientUserName() {
        return clientUserName;
    }

    public void setClientUserName(String clientUserName) {
        this.clientUserName = clientUserName;
    }

    public byte[] getClientPassWord() {
        return clientPassWord;
    }

    public void setClientPassWord(byte[] clientPassWord) {
        this.clientPassWord = clientPassWord;
    }

    public List<KeyValueData> getClientProperty() {
        return clientProperty;
    }

    public void setClientProperty(List<KeyValueData> clientProperty) {
        this.clientProperty = clientProperty;
    }

    public short getConnErrCode() {
        return connErrCode;
    }

    public void setConnErrCode(short connErrCode) {
        this.connErrCode = connErrCode;
    }

    public List<Object> getErrParameter() {
        return errParameter;
    }

    public void setErrParameter(List<Object> errParameter) {
        this.errParameter = errParameter;
    }

    public String getErrDescription() {
        return errDescription;
    }

    public void setErrDescription(String errDescription) {
        this.errDescription = errDescription;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public short getVersion() {
        return version;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public void addInsertMsgCount() {
        this.insertMsgCount.getAndIncrement();
    }

    public void subInsertMsgCount() {
        this.insertMsgCount.getAndDecrement();
    }

    public void addQueryMsgCount() {
        this.queryMsgCount.getAndIncrement();
    }

    public void subQueryMsgCount() {
        this.queryMsgCount.getAndDecrement();
    }

    public boolean isInsertBusy() {
        return insertMsgCount.get() >= Config.getInsertQueueThreshold();
    }

    public boolean isQueryBusy() {
        return queryMsgCount.get() >= Config.getQueryQueueThreshold();
    }

    public AtomicInteger getInsertMsgCount() {
        return insertMsgCount;
    }

    public void setInsertMsgCount(AtomicInteger insertMsgCount) {
        this.insertMsgCount = insertMsgCount;
    }

    public AtomicInteger getQueryMsgCount() {
        return queryMsgCount;
    }

    public void setQueryMsgCount(AtomicInteger queryMsgCount) {
        this.queryMsgCount = queryMsgCount;
    }
}
