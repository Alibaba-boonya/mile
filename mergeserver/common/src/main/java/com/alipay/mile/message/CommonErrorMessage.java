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
public class CommonErrorMessage extends AbstractMessage {

    /**�������  */
    private short        errorCode;
    /**�������  */
    private List<Object> errorParameters = new ArrayList<Object>();
    /**��������  */
    private String       errorDescription;

    public CommonErrorMessage() {
        super();
        setType(MT_COMMON_ERROR);
    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#writeToStream(java.io.DataOutput)
     */
    @Override
    protected void writeToStream(DataOutput os) throws IOException {
        super.writeToStream(os);
        //���� errorCode
        os.writeShort(errorCode);

        //���� errorParameters
        if (errorParameters == null || errorParameters.isEmpty()) {
            os.writeInt(0);

        } else {
            os.writeInt(errorParameters.size());

            for (Object obj : errorParameters) {
                ByteConveror.outPutData(os, obj);
            }
        }
        //���� errorDescription
        os.writeUTF(errorDescription);

    }

    /**
     * @see com.alipay.mile.message.AbstractMessage#readFromStream(java.io.DataInput)
     */
    @Override
    protected void readFromStream(DataInput is) throws IOException {
        super.readFromStream(is);
        //���� errorCode
        errorCode = is.readShort();

        //���� errorParameters
        int arrayLength = is.readInt();

        for (int i = 0; i < arrayLength; i++) {
            errorParameters.add(ByteConveror.getData(is));
        }
        //���� errorDescription
        this.errorDescription = is.readUTF();

    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public List<Object> getErrorParameters() {
        return errorParameters;
    }

    public void setErrorParameters(List<Object> errorParameters) {
        this.errorParameters = errorParameters;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer(29);
        buff.append("ErrorCode:");
        buff.append(errorCode);
        if (errorParameters != null) {
            for (Object obj : errorParameters) {
                buff.append("[");
                buff.append(obj);
                buff.append("]: ");
            }
        }
        buff.append("ErrorDescription");
        buff.append(errorDescription);
        return buff.toString();
    }
}
