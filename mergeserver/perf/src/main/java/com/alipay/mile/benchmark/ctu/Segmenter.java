/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.benchmark.ctu;

import java.util.List;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: Segmenter.java, v 0.1 2012-11-5 ����08:23:35 yuzhong.zhao Exp $
 */
public interface Segmenter {
    /**
     * ��ָ�����ַ������зִʲ���
     * 
     * @param source Դ�ַ���
     * @return �и��Ĵ��б�
     */
    public List<String> split(String source);
}
