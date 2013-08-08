/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.benchmark.ctu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: SimilarityEvaluatorImpl.java, v 0.1 2012-11-5 ����05:31:42 yuzhong.zhao Exp $
 */
public class SimilarityEvaluatorImpl implements SimilarityEvaluator {

    /** �հ׷ָ��ַ� */
    private static final String SEPARATOR         = " ";

    /** �ִ��� */
    private Segmenter           segmenter;

    /** �մ����� */
    private static final int    ZERO_LENGTH       = ZlibUtils.compress("".getBytes()).length;

    /** ���� */
    private static final int    VERY_LARGE_NUMBER = 999;

    /** ��־ */
    private static final Logger logger            = Logger.getLogger(SimilarityEvaluatorImpl.class);

    
    public SimilarityEvaluatorImpl(Segmenter segmenter){
        this.segmenter = segmenter;
    }
    
    
    /** 
     * @see com.alipay.mile.benchmark.ctu.securitydata.common.service.facade.normalization.SimilarityEvaluator#evaluate(java.util.List, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public float evaluate(List<String> words1, List<String> words2) {
        long start = System.currentTimeMillis();
        words1 = (List<String>) (words1 == null ? Collections.emptyList() : words1);
        words2 = (List<String>) (words2 == null ? Collections.emptyList() : words2);
        Set<String> wordSet1 = new HashSet<String>(words1);
        Set<String> wordSet2 = new HashSet<String>(words2);

        float ret = doEvaluate(wordSet1, wordSet2);
        if (logger.isDebugEnabled()) {
            logger.debug("evaluate, Y," + (System.currentTimeMillis() - start) + "ms");
        }
        return ret;
    }

    /** 
     * @see com.alipay.mile.benchmark.ctu.securitydata.common.service.facade.normalization.SimilarityEvaluator#evaluate(java.lang.String, java.lang.String)
     */
    public float evaluate(String str1, String str2) {
        long start = System.currentTimeMillis();
        //��ȫ��ͬ
        if (StringUtils.equals(str1, str2)) {
            return 1;
        }
        Set<String> words1 = preProcess(str1);
        Set<String> words2 = preProcess(str2);
        float ret = doEvaluate(words1, words2);
        if (logger.isDebugEnabled()) {
            logger.debug("evaluate, Y," + (System.currentTimeMillis() - start) + "ms");
        }
        return ret;

    }

    /**
     * �����㷨ʵ��
     * @param words1
     * @param words2
     * @return
     */
    @SuppressWarnings("unchecked")
    private float doEvaluate(Set<String> words1, Set<String> words2) {
        if (words1.containsAll(words2) && words2.containsAll(words1)) {
            return 1;
        }
        //����
        List<String> intersection = new ArrayList<String>(CollectionUtils.intersection(words1,
            words2));
        Collections.sort(intersection);
        //����1 - ����2�Ĳ
        List<String> words1_2 = new ArrayList<String>(CollectionUtils.subtract(words1, words2));
        Collections.sort(words1_2);
        //����2 - ����1�Ĳ
        List<String> words2_1 = new ArrayList<String>(CollectionUtils.subtract(words2, words1));
        Collections.sort(words2_1);
        String x = StringUtils.join(intersection.iterator(), SEPARATOR);
        String p = String.format("%s%s%s", x, SEPARATOR, StringUtils.join(words1_2.iterator(),
            SEPARATOR));
        String t = String.format("%s%s%s", x, SEPARATOR, StringUtils.join(words2_1.iterator(),
            SEPARATOR));
        int lcp = calcuteCompressedLength(p);
        int lct = calcuteCompressedLength(t);
        String compressedPt = ZlibUtils.compress((p + t));
        String compressedTp = ZlibUtils.compress((t + p));
        float unionLength = (compressedPt + compressedTp).length() / 2.0f;
        float common = Math.abs(lcp + lct + ZERO_LENGTH - unionLength);
        Float tmp = safeDivide(common, lcp);
        Float tmp2 = safeDivide(common, lct);
        Float standard = tmp.compareTo(tmp2) < 0 ? tmp : tmp2;
        BigDecimal b = new BigDecimal(standard);
        //��ȷ��С�����6λ
        return b.setScale(6, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * safe divide 
     * @param denominator
     * @param numerator
     * @return
     */
    private float safeDivide(float denominator, int numerator) {
        numerator = numerator == 0 ? VERY_LARGE_NUMBER : numerator;
        float ret = Math.abs(denominator / numerator);
        return ret >= 1.0f ? 1.0f : ret;
    }

    /**
     * ����ǰ���ַ�������Ԥ����
     * <ul>
     * <li>step 1: �ִ�</li>
     * <li>step 2: ȥ��</li>
     * </ul>
     * @param str
     * @return
     */
    private Set<String> preProcess(String str) {
        List<String> words = segmenter.split(str);
        return new HashSet<String>(words);
    }

    /**
     * ����ѹ������ַ�������
     * @param str ��������ַ���
     * @return ѹ������ַ�������/
     */
    private int calcuteCompressedLength(String str) {
        if (StringUtils.isEmpty(str)) {
            return 0;
        }
        int length = ZlibUtils.getCompressedLength(str);
        return Math.abs(length - ZERO_LENGTH);
    }

    public void setSegmenter(Segmenter segmenter) {
        this.segmenter = segmenter;
    }

}
