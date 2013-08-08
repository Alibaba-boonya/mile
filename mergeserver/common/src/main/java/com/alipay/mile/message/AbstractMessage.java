/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author jin.qian
 * @version $Id: AbstractMessage.java,v 0.1 2011-4-6 ����05:38:36 jin.qian Exp $
 */
public abstract class AbstractMessage implements Message {

    /** ��Ϣ���� */
    private int   length;
    /** ��Ϣ���� */
    private short type;
    /** ��Ϣid */
    private int   id;
    /** �汾 */
    private short version;
    
    protected AbstractMessage() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public short getVersion() {
        return this.version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @see com.alipay.mile.message.Message#toBytes()
     */
    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput dos = new DataOutputStream(baos);
        writeToStream(dos);
        int length = baos.size();
        byte[] bytes = baos.toByteArray();

        // ������ɺ��������Ϣ����
        bytes[3] = (byte) (length >>> 0);
        bytes[2] = (byte) (length >>> 8);
        bytes[1] = (byte) (length >>> 16);
        bytes[0] = (byte) (length >>> 24);
        return bytes;
    }

    /**
     * ��Ϣ����
     *
     * @param os
     * @throws IOException
     */
    protected void writeToStream(DataOutput os) throws IOException {
        os.writeInt(0); // length not known now, only used the position
        os.writeShort(getVersion()); // version
        os.writeShort(type); // message type
        os.writeInt(id); // request id
    }

    /**
     * @see com.alipay.mile.message.Message#fromBytes(byte[])
     *
     */
    @Override
    public void fromBytes(byte[] bytes) throws IOException {
        int len = ((bytes[3] & 0xFF) << 0) + ((bytes[2] & 0xFF) << 8) + ((bytes[1] & 0xFF) << 16)
                  + ((bytes[0] & 0xFF) << 24);
        if (len != bytes.length) {
            throw new IllegalArgumentException("Message Bytes Length Error.");
        }
        this.length = len;
        len = bytes.length - 4;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes, 4, len);
        DataInput dis = new DataInputStream(bais);
        readFromStream(dis);
    }

    /**
     * ��Ϣ����
     *
     * @param is
     * @throws IOException
     */
    protected void readFromStream(DataInput is) throws IOException {
        // length already read
        this.version = is.readShort(); // version
        this.type = is.readShort(); // message type
        this.id = is.readInt(); // request id
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        writeToString(sb);
        return sb.toString();
    }

    protected void writeToString(StringBuffer sb) {
        sb.append("��ϢVersion[");
        sb.append(version);
        sb.append("]��Ϣtype[");
        sb.append(type);
        sb.append("]��Ϣid[");
        sb.append(id);
        sb.append("]��Ϣ����[");
        sb.append(length);
        sb.append("]");
    }
}
