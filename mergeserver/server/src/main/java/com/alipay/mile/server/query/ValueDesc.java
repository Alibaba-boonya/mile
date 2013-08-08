/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

/**
 * 
 * @author jin.qian
 * @version $Id: ValueDesc.java, v 0.1 2011-5-10 ����05:49:00 jin.qian Exp $
 */
public class ValueDesc {

    /** ����ֵ */
    public Object value;

    /** sql���� �ַ���ֵ */
    public String valueDesc;

    /** ?��λ�� */
    public int    parmIndex;

    @Override
    public String toString() {
        if (value == null) {
            return "NULL";
        }
        return value.toString();
    }

}
