/**
 * 
 */
package com.alipay.mile.test;

import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alipay.ats.annotation.Feature;
import com.alipay.ats.junit.SpecRunner;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * @author xiaoju.luo
 * @version 
 */
@RunWith(SpecRunner.class)
@Feature("��ѯ�ضϵ�count(*)����")
public class SR621270 extends DocdbTestTools  {
	 /** ��ʱ */
    private int                 timeOut = 5000;
    private int                 num     = 30000;
    private long                cut     = 20000L;
  
 

    @Before
    public void setUp() {
        // ��������
        String sql = "insert into TEST_DAILY TEST_ID=?  GMT_TEST=?";
        Object[] params = new Object[2];

        params[1] = 200L;

        for (int i = 0; i < num; i++) {
            MileInsertResult insertResult;
            params[0] = "127.0.0.9";
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

    @After
    public void tearDown() {
        try {
            // ɾ������
         
        	String sql = "delete from TEST_DAILY indexwhere TEST_ID=?";
            Object[] params = new Object[1];
            params[0] = "127.0.0.9";
            getApplationClientImpl().preDelete(sql, params, timeOut);
          
        } catch (Exception e) {
        	Assert.isFalse(true, "���������쳣");
        }
    }

    @Test
    public void testCount() {
    	String sql = "select count(*) from TEST_DAILY indexwhere TEST_ID=?";
        Object[] params = new Object[1];
        params[0] = "127.0.0.9";
        MileQueryResult queryResult = null;
        try {
            queryResult =  getApplationClientImpl().preQueryForList(sql, params, timeOut);   
                Logger.info("��ѯ���"+queryResult);
        } catch (Exception e) {
        	Assert.isFalse(true, "���������쳣");
        }
       Assert.areEqual(cut, queryResult.getQueryResult().get(0).get("COUNT (*)"),"Ԥ�ڽ��");

    }

}
