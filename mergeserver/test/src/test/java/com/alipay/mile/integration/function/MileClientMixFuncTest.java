/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.integration.function;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;
import com.alipay.mile.integration.MileClientAbstract;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: MileClientMixFuncTest.java, v 0.1 2011-7-27 ����09:37:08 yuzhong.zhao Exp $
 */
public class MileClientMixFuncTest extends MileClientAbstract {
    private static final Logger LOGGER  = Logger.getLogger(MileClientMixFuncTest.class.getName());

    /**��ʱ*/
    private int                 timeOut = 5000;

    @Override
    public void setUp() {
        try {
            super.setUp();
        } catch (UnsupportedEncodingException e) {
            fail();
        }

        // ��������
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
        Object[] params = new Object[4];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";

        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = Long.valueOf(i);
            try {
                insertResult = applationClientImpl.preInsert(sql, params, timeOut);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("docid: " + insertResult.getDocId());
                }
            } catch (Exception e) {
                fail();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Override
    public void tearDown() {
        try {
            // ɾ������
            String sql = "delete from TEST_DAILY indexwhere TEST_IP=?";
            String[] params = new String[1];
            params[0] = "127.0.0.1";
            applationClientImpl.preDelete(sql, params, timeOut);
            super.tearDown();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMix() {
        String sql = "select sum(GMT_TEST), count(GMT_TEST), min(GMT_TEST), max(GMT_TEST), avg(GMT_TEST), variance(GMT_TEST), stddev(GMT_TEST), squaresum(GMT_TEST) from TEST_DAILY indexwhere TEST_IP=?";
        String[] params = new String[1];
        params[0] = "127.0.0.1";
        MileQueryResult queryResult = null;
        try {
            queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            fail();
        }

        assertEquals(Double.valueOf(45), queryResult.getQueryResult().get(0).get("SUM (GMT_TEST)"));
        assertEquals(Long.valueOf(9), queryResult.getQueryResult().get(0).get("MAX (GMT_TEST)"));
        assertEquals(Long.valueOf(0), queryResult.getQueryResult().get(0).get("MIN (GMT_TEST)"));
        assertEquals(Long.valueOf(10), queryResult.getQueryResult().get(0).get("COUNT (GMT_TEST)"));

        assertEquals(Double.valueOf(4.5), queryResult.getQueryResult().get(0).get("AVG (GMT_TEST)"));
        assertEquals(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0).get(
            "STDDEV (GMT_TEST)"));
        assertEquals(Double.valueOf(8.25), queryResult.getQueryResult().get(0).get(
            "VARIANCE (GMT_TEST)"));
        assertEquals(Double.valueOf(285), queryResult.getQueryResult().get(0).get(
            "SQUARESUM (GMT_TEST)"));
    }

}
