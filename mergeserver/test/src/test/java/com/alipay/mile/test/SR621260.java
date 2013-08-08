/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2011 All Rights Reserved.
 */
package com.alipay.mile.test;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alipay.ats.annotation.Feature;
import com.alipay.ats.junit.SpecRunner;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;
import com.alipay.mile.integration.MileClientAbstract;

/**
 * 
 * @author xiaoju.luo
 */
@RunWith(SpecRunner.class)
@Feature("��ѯ�ضϵ�avg����")
public class SR621260 extends DocdbTestTools  {

    /** ��ʱ */
    private int                 timeOut = 5000;
   // private int                 num     = 30000;
    private int                 num     = 3000;

    @Override
    public void setUp() {
        

        // ��������
        String sql = "insert into TEST_DAILY TEST_ID=? GMT_TEST=?";
        Object[] params = new Object[2];

        for (int i = 0; i < num; i++) {
            MileInsertResult insertResult;
            params[0] = "127.0.0.8";
            params[1] = 200L;
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
              
                    Logger.info("docid: " + insertResult.getDocId());
              
            } catch (Exception e) {
            	 Assert.isFalse(true, "���������쳣");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        	 Assert.isFalse(true, "���������쳣");
        }
    }

    @Override
    public void tearDown() {
        try {
            // ɾ������
        	String sql = "delete from TEST_DAILY indexwhere TEST_ID=?";
            Object[] params = new Object[1];
            params[0] = "127.0.0.8";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
        	 Assert.isFalse(true, "���������쳣");
        }
    }

    @Test
    public void testAvg() {

    	String sql = "select avg(GMT_TEST) from TEST_DAILY indexwhere TEST_ID=?";
        Object[] params = new Object[1];
        params[0] = "127.0.0.8";
        MileQueryResult queryResult = null;
        try {
            queryResult =  getApplationClientImpl().preQueryForList(sql, params, timeOut);    
                Logger.info("�����"+queryResult);
        } catch (Exception e) {
        
        	 Assert.isFalse(true, "���������쳣");
        }
         System.out.println("��ѯ���"+queryResult.getQueryResult().size());
         Assert.areEqual(200.0, queryResult.getQueryResult().get(0).get("AVG (GMT_TEST)"),"Ԥ�ڽ��");

    }
}
