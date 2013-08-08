/**
 * created since 2012-3-16
 */
package com.alipay.mile.integration.mix;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.alipay.mile.client.SqlClientTemplate;
import com.alipay.mile.client.result.MileDeleteResult;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;
import com.alipay.mile.integration.MileClientAbstract;

/**
 * @author xiaoju.luo
 * @version $Id: SqlClientTemplateTest.java,v 0.1 2012-3-16 ����06:38:20 xiaoju.luo Exp $
 */
@Ignore
public class MileClientpreCtuClusterQueryTest extends MileClientAbstract {

    private static final Logger LOGGER  = Logger.getLogger(MileClientpreCtuClusterQueryTest.class
                                            .getName());
    SqlClientTemplate           sqlClient;
    private int                 timeOut = 5000;

    @Before
    public void setUp() {
        try {
            super.setUp();
            sqlClient = (SqlClientTemplate) applationClientImpl;
        } catch (UnsupportedEncodingException e1) {
            Assert.fail();
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
                params[0] = new String("a" + i);
                params[8] = new Date().getTime();
                params[9] = Long.valueOf(i);
                MileInsertResult insertResult = sqlClient.preInsert(sql, params, timeOut);
                Assert.assertNotNull(insertResult);
            }
        } catch (Exception e) {
            LOGGER.error("�쳣����", e);
            Assert.fail();
        }

    }

    
            @Test
    /*
     * ���Դ��������Ĳ������ӿڷ��ؽ������
     */
    public void testPreCtuClusterQuery() {
        String tableName = "TEST";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "a1");
        selectFields.add(1, "b1");
        selectFields.add(2, "value");
        selectFields.add(3, "c1");
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
        String orderField = "value";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);

        try {
            MileQueryResult queryResult = sqlClient.preCtuClusterQuery(tableName, selectFields,
                condition, clusterField, topField, orderField, orderType, limit, offset, params1,
                timeOut);
            Assert.assertNotNull(queryResult);
            String str = "";
            for (int i = 0; i < 7; i++) {
                for (String key : queryResult.getQueryResult().get(i).keySet()) {
                    str += queryResult.getQueryResult().get(i).get(key).toString();
                }
            }
            Assert.assertEquals("ccbb8a8ccbb7a7ccbb6a6ccbb5a5ccbb4a4ccbb3a3ccbb2a2", str);
        } catch (Exception e) {
            //LOGGER.error("�쳣", e);
            Assert.fail();
        }
    }

    @Test
    /*
     * ���Դ���List����Ϊ��ֵ���ӿڵĲ���
     */
    public void testPreCtuClusterQueryEmptyException() {
        String tableName = "TEST";
        List<String> selectFields = new ArrayList<String>();

        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
        String orderField = "value";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        MileQueryResult queryResult = null;
        try {
            queryResult = sqlClient.preCtuClusterQuery(tableName, selectFields, condition,
                clusterField, topField, orderField, orderType, limit, offset, params1, timeOut);
            Assert.assertNotNull(queryResult);
           // System.out.println(queryResult);
        } catch (Exception e) {
          //  LOGGER.error("�쳣", e);
            Assert.assertNull(queryResult);
        }
    }

    @Test
    /*
     * ���Դ������Ϊnull���ӿڵ��쳣����
     */
    public void testPreCtuClusterQueryNullException() {
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
        String orderField = "value";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = Long.valueOf(1);
        params1[2] = Long.valueOf(9);
        MileQueryResult queryResult = null;
        try {
            queryResult = sqlClient.preCtuClusterQuery(null, null, condition, clusterField,
                topField, orderField, orderType, limit, offset, params1, timeOut);
            Assert.assertNotNull(queryResult);
        } catch (Exception e) {
           // LOGGER.error("�쳣", e);
            Assert.assertNull(queryResult);
        }
    }
        

    @Test
    /*
     * ���Բ�ѯ���ֶε��������͸�������������Ͳ�һ�����ӿڵ��쳣���ԣ��жϷ��صĲ�ѯ����б�Ϊ�� 
     */
    public void testPreCtuClusterQueryTypeException() {
        String tableName = "TEST";
        List<String> selectFields = new ArrayList<String>();
        selectFields.add(0, "a1");
        selectFields.add(1, "b1");
        selectFields.add(2, "value");
        selectFields.add(3, "c1");
        String condition = "indexwhere c1=? where value>? and value<?";
        String clusterField = "a1";
        String topField = "max(value)";
        String orderField = "value";
        boolean orderType = false;
        int limit = 9;
        int offset = 0;
        Object[] params1 = new Object[3];
        params1[0] = "cc";
        params1[1] = new Double(1);
        params1[2] = new Double(9);
        MileQueryResult queryResult = null;
        try {
            queryResult = sqlClient.preCtuClusterQuery(tableName, selectFields, condition,
                clusterField, topField, orderField, orderType, limit, offset, params1, timeOut);
            //�жϷ��صĲ�ѯ����б�Ϊ�� 
            boolean result = queryResult.getQueryResult().isEmpty();
            Assert.assertTrue(result);
        } catch (Exception e) {
           // LOGGER.error("�쳣", e);
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
        } catch (Exception e) {
          //  LOGGER.error("", e);
            Assert.fail();
        }
    }
}
