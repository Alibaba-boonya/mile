/**
 * created since 2012-7-24
 */

package com.alipay.mile.integration.insert;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;
import com.alipay.mile.integration.MileClientAbstract;

/**
 * ����֤group by���ܵ�����
 * @author xiaoju.luo
 * @version $Id: MileClientPreInsertCount.java,v 0.1 2012-7-24 ����05:17:12 xiaoju.luo Exp $
 */
public class MileClientPreInsertCount extends MileClientAbstract {

    private static final Logger LOGGER  = Logger
                                            .getLogger(MileClientPreInsertCount.class.getName());

    /**��ʱ*/
    private int                 timeOut = 5000;

    @Override
    public void setUp() {
        try {
            super.setUp();
        } catch (UnsupportedEncodingException e) {
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
    public void testPrepareInsert() {
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";

        Object[] params = new Object[4];
        // params[0] = "12345";
        params[0] = "abce";
        params[1] = "ctumile";
        params[2] = "127.0.0.1";
        params[3] = Long.valueOf(6);
        for (int i = 0; i < 5000; i++) {
            MileInsertResult insertResult;
            try {
                insertResult = applationClientImpl.preInsert(sql, params, timeOut);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("docid: " + insertResult.getDocId());
                }
                assertTrue(insertResult.getDocId() > 0);
            } catch (Exception e) {
                LOGGER.error(e);
                fail();
            }

        }
    }

    @Test
    public void testCount() {
        String sql = "select count(*) as a from TEST_DAILY indexwhere TEST_ID=?";
        Object[] params = new Object[1];
        //params[0] = "12345";
        params[0] = "abce";
        try {
            MileQueryResult queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);

            assertEquals(Long.valueOf(5000), queryResult.getQueryResult().get(0).get("a"));
        } catch (Exception e) {
            LOGGER.error(e);
            fail();
        }

    }

    @Test
    public void testGroupBy() {
        // String sql = "select TEST_ID,TEST_NAME,TEST_IP,GMT_TEST from TEST_DAILY indexwhere TEST_ID=? group by EXTERNAL_ID";
        String sql = "select TEST_IP,count(*) as a,sum(GMT_TEST) as b from TEST_DAILY indexwhere TEST_ID=? group by TEST_IP";

        Object[] params = new Object[1];
        params[0] = "abce";
        try {
            long startTime = System.currentTimeMillis();
            MileQueryResult queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
            long endTime = System.currentTimeMillis();
            long spend = endTime - startTime;
            assertEquals(Long.valueOf(5000), queryResult.getQueryResult().get(0).get("a"));
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("����ʱ��Ϊ��" + spend + "ms");
            }
        } catch (Exception e) {
            LOGGER.error(e);
            fail();
        }

    }
}
