/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2011 All Rights Reserved.
 */
package com.alipay.mile.server.query;

/**
 * ��ʱ�����
 * @author jin.qian
 * @version $Id: TimeHint.java, v 0.1 2011-5-10 ����05:44:15 jin.qian Exp $
 */
public class TimeHint {

    /**�δ�����Сʱ��  */
    public long startCreateTime = 0;
    /** �δ������ʱ�� */
    public long endCreateTime   = Long.MAX_VALUE;
    /**�θ�����Сʱ��  */
    public long startUpdateTime = 0;
    /**�θ������ʱ��  */
    public long endUpdateTime   = Long.MAX_VALUE;

}
