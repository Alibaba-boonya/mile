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

import com.alipay.mile.util.ByteConveror;

/**
 * @author jin.qian
 * @version $Id: ClientConnRespError.java,v 0.1 2011-4-6 ����05:39:03 jin.qian Exp $
 */
public class ClientConnRespError extends AbstractMessage {

    /** ���Ӵ������ */
    private short        connErrCode;
    /** ������� */
    private List<Object> errParameter = new ArrayList<Object>();
    /** �������� */
    private String       errDescription;

    /**
     * ������
     */
    public ClientConnRespError() {
        super();
        setType(MT_MC_CONN_RS_ERR);
    }

    public short getConnErrCode() {
        return connErrCode;
    }

    public void setConnErrCode(short connErrCode) {
        this.connErrCode = connErrCode;
    }

    public String getErrDescription() {
        return errDescription;
    }

    public void setErrDescription(String errDescription) {
        this.errDescription = errDescription;
    }

    public List<Object> getErrParameter() {
        return errParameter;
    }

    public void setErrParameter(List<Object> errParameter) {
        this.errParameter = errParameter;
    }

    /** 
     * @see com.alipay.mile.message.AbstractMessage#writeToStream(java.io.DataOutput)
     */
    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);
        //���� connErrCode
        os.writeShort(connErrCode);
        //���� errParameter
        if (errParameter == null || errParameter.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(errParameter.size());
            for (Object obj : errParameter) {
                ByteConveror.outPutData(os, obj);
            }
        }
        //���� errDescription
        byte[] data = errDescription.getBytes("utf-8");
        os.writeShort(data.length);
        os.write(data);
    }

    /** 
     * @see com.alipay.mile.message.AbstractMessage#readFromStream(java.io.DataInput)
     */
    @Override
    protected void readFromStream(DataInput is) throws IOException {
        super.readFromStream(is);
        //��ȡconnErrCode
        connErrCode = is.readShort();
        //��ȡerrParameter
        int j = is.readInt();
        for (int i = 0; i < j; i++) {
            errParameter.add(ByteConveror.getData(is));
        }
        //��ȡerrDescription
        short strlen = is.readShort();
        byte[] data = new byte[strlen];
        is.readFully(data, 0, strlen);
        String str = new String(data, 0, strlen, "utf-8");
        this.errDescription = str;
    }

}
