/**
 * created since 2012-3-19
 */
package com.alipay.mile.integration.mix;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alipay.mile.client.SqlClientTemplate;
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.integration.MileClientAbstract;
import com.alipay.mile.mileexception.SqlExecuteException;

/**
 * @author xiaoju.luo
 * @version $Id: preCtuClusterCountQuery.java,v 0.1 2012-3-19 ����09:29:41
 *          xiaoju.luo Exp $
 */
public class CTUpreCtuClusterCountQueryTest extends MileClientAbstract {

    private static final Logger LOGGER  = Logger.getLogger(CTUpreCtuClusterCountQueryTest.class
                                            .getName());
    SqlClientTemplate           sqlClient;
    private int                 timeOut = 1000;

    @Before
    public void setUp() {
        try {
            super.setUp();
            sqlClient = (SqlClientTemplate) applationClientImpl;
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("ִ���쳣", e1);
        }

        String sql = "insert into TEST_DAILY TEST_ID=? TEST_IP=? TEST_NAME=? exid=? clid=? eename=? g1=? U=? time=? GMT_TEST=?";
        Object[] params = new Object[10];
        params[1] = "bb";
        params[2] = "cc";
        params[3] = "dd";
        params[4] = "ee";
        params[5] = "ff";
        params[6] = "gg";
        params[7] = "name";

        try {
            for (int i = 0; i < 10; i++) {
                params[0] = new String("aa" + i);
                params[8] = new Date().getTime();
                params[9] = Long.valueOf(i);
                MileInsertResult insertResult = sqlClient.preInsert(sql, params, timeOut);
                Assert.assertNotNull(insertResult);

            }
        } catch (Exception e) {
            LOGGER.error("SQLִ�д���", e);
            Assert.fail();
        }
    }

    @Test
    /*
     * ���Դ��������Ĳ������ӿڵĹ�������
     */
    public void testPreCtuClusterCountQuery() {
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388889343,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        try {
            int queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, params1, timeOut);
            // System.out.println("���صĽ��������Ϊ��" + queryResult);
            Assert.assertEquals(7, queryResult);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
            Assert.fail();

        }
    }

    @Test
    /*
     * ���Դ������cluster�Ĳ����쳣��������Ҫ�׳��쳣
     */
    public void testPreMulClusterCountQuery() {
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1338889343,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID,TEST_IP,TEST_NAME,exid,clid,eename";

        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        int queryResult = 0;
        try {
            queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, params1, timeOut);
            Assert.fail();
        } catch (SqlExecuteException e) {
            LOGGER.error("SqlExecuteException", e);
            Assert.assertEquals(0, queryResult);

        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    /*
     * ���Դ���Ĳ�������Ϊ��ʱ���������СΪ0
     */
    public void testPreCtuClusterCountQueryEmptyException() {
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388885390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        // params1[0] = "cc";
        // params1[1] = Long.valueOf(1);
        // params1[2] = Long.valueOf(9);
        try {
            int queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, params1, timeOut);
            Assert.assertEquals(0, queryResult);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
            Assert.fail();
        }

    }

    @Test
    /*
     * ���Դ���Ĳ�������Ϊnullʱ���ӿڱ��쳣
     */
    public void testPreCtuClusterCountQueryNullException() {
        String tableName = "";
        String condition = "seghint(0,1388815390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        int queryResult = 0;
        try {
            queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, null, timeOut);
            Assert.fail();
        } catch (Exception e) {
            LOGGER.error("SqlExecuteException", e);
            Assert.assertEquals(0, queryResult);
        }
    }

    @Test
    /*
     * ���Դ���Ĳ���stringΪ�ո�ʱ����� mile���﷨���󣬽ӿڱ��쳣
     */
    public void testPreCtuClusterCountQuerySyntaxException() {

        String condition = "";
        String clusterField = "";
        String topField = "";
        int queryResult = 0;
        try {
            queryResult = sqlClient.preCtuClusterCountQuery(null, condition, clusterField,
                topField, null, timeOut);
            Assert.fail();
        } catch (Exception e) {
            LOGGER.error("SqlExecuteException", e);
            Assert.assertEquals(0, queryResult);
        }
    }

    @Test
    /*
     * ���Դ���Ĳ������������Ͳ��ǲ���ʱ���������ͣ��ӿڷ��ؽ��Ϊ0���쳣������
     */
    public void testPreCtuClusterCountQueryTypeException() {
        String tableName = "TEST_DAILY";
        String condition = "seghint(0,1388815390,0,0) indexwhere TEST_NAME=? where GMT_TEST>? and GMT_TEST<?";
        String clusterField = "TEST_ID";
        String topField = "max(GMT_TEST)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = new Double(1);
        params1[2] = new Double(9);

        int queryResult = 0;
        try {
            queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, params1, timeOut);
            Assert.assertEquals(0, queryResult);
        } catch (Exception e) {
            LOGGER.error("SqlExecuteException", e);
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        try {
            String sql = "delete from TEST_DAILY indexwhere TEST_NAME=?";
            Object[] params2 = new Object[1];
            params2[0] = "cc";
            MileDeleteResult dResult = sqlClient.preDelete(sql, params2, timeOut);
            Assert.assertNotNull(dResult);
            super.tearDown();
        } catch (Exception e) {
            Assert.fail();
        }
    }

}
