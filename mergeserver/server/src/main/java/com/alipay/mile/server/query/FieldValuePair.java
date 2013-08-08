/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

import com.alipay.mile.FieldDesc;

public class FieldValuePair {
    /** ������ */
    public FieldDesc field;
    /**  ֵ*/
    public ValueDesc value;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("������ [" + field.toString() + "], " + "��ֵ [" + value.toString() + "]\n");
        return sb.toString();
    }
}
