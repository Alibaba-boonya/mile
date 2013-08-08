/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.integration.select;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
 * @version $Id: MileClientDochintSelectTest.java, v 0.1 2011-7-29 ����05:20:09
 *          yuzhong.zhao Exp $
 */
public class MileClientDochintSelectTest extends MileClientAbstract {
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
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
        Object[] params = new Object[5];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";

        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[0] = String.valueOf(i % 2);
            params[3] = Long.valueOf(i);
            try {
                insertResult = applationClientImpl.preInsert(sql, params, timeOut);
                docidsList.add(insertResult.getDocId());
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
    public void testDocHintSelect() {
        String sql = "select TEST_ID, CIENT_IP, TEST_NAME, GMT_TEST from TEST_DAILY DOCHINT mile_doc_id=";
        Object[] params = null;
        try {
            for (int i = 0; i < 1; i++) {
                MileQueryResult queryResult = applationClientImpl.preQueryForList(sql
                                                                                  + docidsList
                                                                                      .get(i),
                    params, timeOut);
                List<Map<String, Object>> resultList = queryResult.getQueryResult();
                assertEquals(String.valueOf(i % 2), resultList.get(0).get("TEST_ID"));
                assertEquals(Long.valueOf(i), resultList.get(0).get("GMT_TEST"));
            }
        } catch (Exception e) {
            fail();
        }
    }
}
