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
 * @version $Id: ClientConnectMessage.java,v 0.1 2011-4-6 ����05:38:54 jin.qian
 *          Exp $
 */
public class ClientConnectMessage extends AbstractMessage {

    /** �û��� */
    private String             userName;
    /** ���� */
    private byte[]             passWord;
    /** client ���� */
    private List<KeyValueData> clientProperty = new ArrayList<KeyValueData>();

    public ClientConnectMessage() {
        super();
        setType(MT_CM_CONN);
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

    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);
        // �����û���
        byte[] data = userName.getBytes("utf-8");
        os.writeShort(data.length);
        os.write(data);
        // �������
        os.writeInt(passWord.length);
        os.write(passWord);
        // ����clientProperty ����
        if (clientProperty == null || clientProperty.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(clientProperty.size());
            for (KeyValueData keyValueData : clientProperty) {
                keyValueData.writeToStream(os);
            }
        }
    }

    @Override
    protected void readFromStream(DataInput is) throws IOException {
        super.readFromStream(is);
        // ��ȡ�û���
        short strlen = is.readShort();
        byte[] data = new byte[strlen];
        is.readFully(data, 0, strlen);
        String str = new String(data, 0, strlen, "utf-8");
        this.userName = str;
        // ��ȡ����
        int passlen = is.readInt();
        byte[] pass = new byte[passlen];
        is.readFully(pass, 0, passlen);
        this.passWord = pass;
        // ��ȡclientProperty
        int j = is.readInt();

        for (int i = 0; i < j; i++) {
            KeyValueData keyValueData = new KeyValueData();
            keyValueData.readFromStream(is);
            clientProperty.add(keyValueData);
        }
    }
}
