/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alipay.mile.util.ByteConveror;

/**
 * ������
 * 
 * @author huabing.du
 * @version $Id: FieldDesc.java, v 0.1 2011-5-10 ����05:09:59 huabing.du Exp $
 */
public class FieldDesc {

    /** �������ܺ���ʱ��ת����sum(fieldName) */
    public String     fieldName;
    /** �б��� */
    public String     aliseName;
    /** ������ */
    public String     functionName;
    /** ����ID */
    public byte       functionId;
    /** �ο����� */
    public String     refColumnName;
    /** WITHIN ���� */
    public Expression withinExpr;

    // with out within expression
    public void commonPartToStream(DataOutput os) throws IOException {
        if (isComputeField()) {
            // д��������
            os.writeByte(Constants.FUNC_FIELD);
            // д�뺯������
            byte id = functionId;
            // the highest bit is within expression mask.
            if (null != withinExpr) {
                os.writeByte(id + 128);
            } else {
                os.writeByte(id);
            }
            // д������
            ByteConveror.writeString(os, fieldName);
            // д���б���
            ByteConveror.writeString(os, aliseName);
            // д��ref����
            ByteConveror.writeString(os, refColumnName);
            // no within expression
        } else {
            // д��������
            os.writeByte(Constants.GENERAL_FIELD);
            // д������
            ByteConveror.writeString(os, fieldName);
            // д���б���
            ByteConveror.writeString(os, aliseName);
        }
    }

    // with within expression
    public void writeToStream(DataOutput os, Map<Object, List<Object>> paramBindMap)
                                                                                    throws IOException {
        commonPartToStream(os);
        if (null != withinExpr) {
            os.writeInt(withinExpr.size);
            withinExpr.postWriteToStream(os, paramBindMap);
        }
    }

    // no within expression
    public void commonPartFromStream(DataInput is) throws IOException {
        byte type = is.readByte();
        if (type == Constants.FUNC_FIELD) {
            functionId = is.readByte();
            if ((functionId & 0x80) != 0)
                functionId &= ~0x80;
            this.fieldName = ByteConveror.readString(is);
            this.aliseName = ByteConveror.readString(is);
            this.refColumnName = ByteConveror.readString(is);
        } else {
            this.fieldName = ByteConveror.readString(is);
            this.aliseName = ByteConveror.readString(is);
        }
    }

    public boolean isComputeField() {
        return StringUtils.isNotBlank(functionName);
    }

    @Override
    public int hashCode() {
        if (null == withinExpr) {
            return fieldName.hashCode();
        } else {
            return fieldName.hashCode() + withinExpr.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FieldDesc) {
            FieldDesc another = (FieldDesc) obj;
            if (withinExpr == another.withinExpr) {
                return fieldName.equals(another.fieldName);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("���� [" + this.fieldName + "]");
        return sb.toString();
    }
}
