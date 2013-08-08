/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.benchmark.ctu;

import java.util.List;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: SimilarityEvaluator.java, v 0.1 2012-11-5 ����05:30:47 yuzhong.zhao Exp $
 */
public interface SimilarityEvaluator {

    /**
     * �������ַ����������ƶ�����, ����ֵ������[0,1]��Խ����1��ʾԽ���ƣ�Խ����0��ʾԽ������
     * 
     * @param str1 ��һ���ַ���
     * @param str2 �ڶ����ַ���
     * @return ���ƶ�������
     */
    public float evaluate(String str1, String str2);

    /**
     * �Էֺôʵ������ַ����������ƶ�����, ����ֵ������[0,1]��Խ����1��ʾԽ���ƣ�Խ����0��ʾԽ������
     * @param words1 �ַ���1�ķִ��б�
     * @param words2 �ַ���2�ķִ��б�
     * @return ���ƶ�������
     */
    public float evaluate(List<String> words1, List<String> words2);
}
