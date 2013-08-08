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
import org.junit.Ignore;
import org.junit.Test;

import com.alipay.mile.client.SqlClientTemplate;
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.integration.MileClientAbstract;

/**
 * @author xiaoju.luo
 * @version $Id: preCtuClusterCountQuery.java,v 0.1 2012-3-19 ����09:29:41 xiaoju.luo Exp $
 */
@Ignore
public class MileClientpreCtuClusterCountQueryTest extends MileClientAbstract {

    private static final Logger LOGGER  = Logger
                                            .getLogger(MileClientpreCtuClusterCountQueryTest.class
                                            .getName());
    SqlClientTemplate           sqlClient;
    private int                 timeOut = 5000;

    @Before
    public void setUp() {
        try {
            super.setUp();
            sqlClient = (SqlClientTemplate) applationClientImpl;
        } catch (UnsupportedEncodingException e1) {
            LOGGER.error("ִ���쳣", e1);
        }
        String sql = "insert into TEST a1=? b1=? c1=? d1=? e1=? f1=? g1=? U=? time=? value=?";
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
               // System.out.println(insertResult.getDocId());
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
        String tableName = "TEST";
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        try {
            int queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, params1, timeOut);
            Assert.assertEquals(7, queryResult);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
            Assert.fail();

        }
    }

    @Test
    /*
     * ���Դ���Ĳ�������Ϊ��ʱ���������СΪ0
     */
    public void testPreCtuClusterCountQueryEmptyException() {
        String tableName = "TEST";
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
        Object[] params1 = new Object[3];
        //        params1[0] = "cc";
        //        params1[1] = Long.valueOf(1);
        //        params1[2] = Long.valueOf(9);
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
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
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
     * ���Դ���Ĳ���string��Ϊ�ո��ʱ�����mile�﷨���󣬽ӿ��쳣
     */
    public void testPreCtuClusterCountQuerySyntaxException() {
        String tableName = "";
        String condition = "";
        String clusterField = "";
        String topField = "";
        Object[] params1 = new Object[3];
        int queryResult = 0;
        try {
            queryResult = sqlClient.preCtuClusterCountQuery(tableName, condition, clusterField,
                topField, params1, timeOut);
            Assert.fail();
            Assert.assertEquals(7, queryResult);
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
        String tableName = "TEST";
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
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
            String sql = "delete from TEST indexwhere c1=?";
            Object[] params2 = new Object[1];
            params2[0] = "cc";
            MileDeleteResult dResult = sqlClient.preDelete(sql, params2, timeOut);
            Assert.assertNotNull(dResult);
            super.tearDown();
            //applationClientImpl.close();
        } catch (Exception e) {
            Assert.fail();
        }
    }

}
