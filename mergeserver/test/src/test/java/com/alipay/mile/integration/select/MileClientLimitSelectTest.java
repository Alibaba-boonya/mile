/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.integration.select;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;
import com.alipay.mile.integration.MileClientAbstract;
import com.alipay.mile.integration.orderby.MileClientLimitOrderTest;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: MileClientLimitSelectTest.java, v 0.1 2011-7-29 ����05:38:04 yuzhong.zhao Exp $
 */
public class MileClientLimitSelectTest extends MileClientAbstract {
    private static final Logger LOGGER     = Logger.getLogger(MileClientLimitOrderTest.class
                                               .getName());

    /** ��ʱ */
    private int                 timeOut    = 5000;

    List<Long>                  docidsList = new ArrayList<Long>();

    @Override
    public void setUp() {
        try {
            super.setUp();
        } catch (UnsupportedEncodingException e) {
            fail();
        }

        // ��������
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? 11=?";
        Object[] params = new Object[5];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";

        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = new Date().getTime();
            params[4] = String.valueOf(i);
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
    public void testLimitSelect() {
        String sql = "select TEST_NAME from TEST_DAILY indexwhere TEST_IP=? limit 1";
        Object[] params = new Object[1];
        params[0] = "127.0.0.1";

        try {
            MileQueryResult queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();

            assertEquals(1, resultList.size());
            assertEquals("milemac", resultList.get(0).get("TEST_NAME"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testLimitOffsetSelect() {
        String sql = "select TEST_NAME from TEST_DAILY indexwhere TEST_IP=? limit 3 offset 1";
        Object[] params = new Object[1];
        params[0] = "127.0.0.1";

        try {
            MileQueryResult queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();

            assertEquals(2, resultList.size());
            assertEquals("milemac", resultList.get(0).get("TEST_NAME"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testFailLimitOffsetSelect() {
        String sql = "select TEST_NAME from TEST_DAILY indexwhere TEST_IP=? limit 3 offset 3";
        Object[] params = new Object[1];
        params[0] = "127.0.0.1";

        try {
            MileQueryResult queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
            List<Map<String, Object>> resultList = queryResult.getQueryResult();
            assertEquals(0, resultList.size());
        } catch (Exception e) {
            fail();
        }
    }

}
