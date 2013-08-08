/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jin.qian
 * @version $Id: ClientReConnectMessage.java,v 0.1 2011-4-6 ����05:39:21 jin.qian Exp $
 */
public class ClientReConnectMessage extends AbstractMessage {

    /** sessionID */
    private int                sessionID;
    /** �û��� */
    private String             userName;
    /** ����  */
    private byte[]             passWord;
    /** client���� */
    private List<KeyValueData> clientProperty = new ArrayList<KeyValueData>();

    /**
     * ������
     */
    public ClientReConnectMessage() {
        super();
        setType(MT_CM_RE_CONN);
    }

    /** 
     * @see com.alipay.mile.message.AbstractMessage#writeToStream(java.io.DataOutput)
     */
    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);
        //���� sessionID
        os.writeInt(sessionID);
        //���� userName
        byte[] data = userName.getBytes("utf-8");
        os.writeShort(data.length);
        os.write(data);
        //���� passWord
        os.writeInt(passWord.length);
        os.write(passWord);
        //���� clientProperty
        if (clientProperty == null || clientProperty.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(clientProperty.size());
            for (KeyValueData keyValueData : clientProperty) {
                keyValueData.writeToStream(os);
            }
        }
    }

    /** 
     * @see com.alipay.mile.message.AbstractMessage#readFromStream(java.io.DataInput)
     */
    @Override
    protected void readFromStream(DataInput is) throws IOException {
        super.readFromStream(is);
        //���� sessionID
        this.sessionID = is.readInt();
        //���� userName
        short strlen = is.readShort();
        byte[] data = new byte[strlen];
        is.readFully(data, 0, strlen);
        String str = new String(data, 0, strlen, "utf-8");
        this.userName = str;
        //���� passWord
        int passlen = is.readInt();
        byte[] pass = new byte[passlen];
        is.readFully(pass, 0, passlen);
        this.passWord = pass;
        //���� clientProperty
        int j = is.readInt();
        for (int i = 0; i < j; i++) {
            KeyValueData keyValueData = new KeyValueData();
            keyValueData.readFromStream(is);
            clientProperty.add(keyValueData);
        }
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getSessionID() {
        return sessionID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getPassWord() {
        return passWord;
    }

    public void setPassWord(byte[] passWord) {
        this.passWord = passWord;
    }

    public List<KeyValueData> getClientProperty() {
        return clientProperty;
    }

    public void setClientProperty(List<KeyValueData> clientProperty) {
        this.clientProperty = clientProperty;
    }

}
