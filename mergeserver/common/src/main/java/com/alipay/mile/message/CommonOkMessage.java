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
 * @version $Id: CommonErrorMessage.java,v 0.1 2011-4-6 ����05:39:33 jin.qian Exp $
 */
public class CommonOkMessage extends AbstractMessage {

    /**�������  */
    private short        okCode;
    /**�������  */
    private List<Object> okParameters = new ArrayList<Object>();
    /**��������  */
    private String       okDescription;

    public CommonOkMessage() {
        super();
        setType(MT_COMMON_OK);
    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#writeToStream(java.io.DataOutput)
     */
    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);
        //���� errorCode
        os.writeShort(okCode);
        //���� errorParameters
        if (okParameters == null || okParameters.isEmpty()) {
            os.writeInt(0);

        } else {
            os.writeInt(okParameters.size());
            for (Object obj : okParameters) {
                ByteConveror.outPutData(os, obj);
            }
        }
        //���� errorDescription
        os.writeUTF(okDescription);
    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#readFromStream(java.io.DataInput)
     */
    @Override
    protected void readFromStream(DataInput is) throws IOException {
        super.readFromStream(is);
        //���� errorCode
        okCode = is.readShort();
        //���� errorParameters
        int arrayLength = is.readInt();
        for (int i = 0; i < arrayLength; i++) {
            okParameters.add(ByteConveror.getData(is));
        }
        //���� errorDescription
        this.okDescription = is.readUTF();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer(26);
        buff.append("okCode:");
        buff.append(okCode);
        if (okParameters != null) {
            for (Object obj : okParameters) {
                buff.append("[");
                buff.append(obj);
                buff.append("]: ");
            }
        }
        buff.append("ErrorDescription");
        buff.append(okDescription);
        return buff.toString();
    }

    public short getOkCode() {
        return okCode;
    }

    public void setOkCode(short okCode) {
        this.okCode = okCode;
    }

    public List<Object> getOkParameters() {
        return okParameters;
    }

    public void setOkParameters(List<Object> okParameters) {
        this.okParameters = okParameters;
    }

    public String getOkDescription() {
        return okDescription;
    }

    public void setOkDescription(String okDescription) {
        this.okDescription = okDescription;
    }
}
