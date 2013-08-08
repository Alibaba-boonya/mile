/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alipay.mile.util.ByteConveror;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: AccessSqlExeErrorMessage.java, v 0.1 2011-7-7 ����03:10:36
 *          yuzhong.zhao Exp $
 */
public class AccessSqlExeErrorMessage extends AbstractMessage {

    /** SQLErrCode */
    private short        sqlErrorCode;

    /** errParameter ������� */
    private List<Object> errParameter = new ArrayList<Object>();

    /** �������� */
    private String       errDescription;

    /**
     * ������
     */
    public AccessSqlExeErrorMessage() {
        super();
        setType(MT_DM_SQL_EXC_ERROR);
    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#writeToStream(java.io.DataOutput)
     */
    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);
        // ����SQLErrCode
        os.writeShort(sqlErrorCode);

        // ����errParameter
        if (errParameter == null || errParameter.isEmpty()) {
            os.writeInt(0);
        } else {
            os.writeInt(errParameter.size());
            for (Object obj : errParameter) {
                ByteConveror.outPutData(os, obj);
            }
        }
        // ����errDescription
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
        // ����SQLErrCode
        sqlErrorCode = is.readShort();
        // ����errParameter
        int j = is.readInt();
        for (int i = 0; i < j; i++) {
            errParameter.add(ByteConveror.getData(is));
        }
        // ����errDescription
        int strlen = is.readInt();
        byte[] data = new byte[strlen];
        is.readFully(data, 0, strlen);
        String str = new String(data, 0, strlen, "utf-8");
        this.errDescription = str;
    }

    public short getSQLErrCode() {
        return sqlErrorCode;
    }

    public void setSQLErrCode(short sqlErrorCode) {
        this.sqlErrorCode = sqlErrorCode;
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

    /**
     * ��������
     * 
     * @return
     */
    public String toString() {
        StringBuffer strb = new StringBuffer(50);
        strb.append("SQLErrCode: [");
        strb.append(sqlErrorCode);
        strb.append("] ErrDescription :[");
        strb.append(errDescription);
        strb.append("] ");
        for (Object obj : errParameter) {
            strb.append("[");
            strb.append(obj);
            strb.append("]: ");
        }
        return strb.toString();
    }
}
