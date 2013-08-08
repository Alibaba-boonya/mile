/**
 * 
 */
package com.alipay.mile.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alipay.ats.annotation.Feature;
import com.alipay.ats.annotation.Priority;
import com.alipay.ats.annotation.Subject;
import com.alipay.ats.annotation.Tester;
import com.alipay.ats.enums.PriorityLevel;
import com.alipay.ats.junit.SpecRunner;
import com.alipay.mile.client.result.MileInsertResult;
import com.alipay.mile.client.result.MileQueryResult;

/**
 * @author xiaoju.luo
 * @version $Id: SR621160.java,v 0.1 2012-11-7 ����05:57:23 xiaoju.luo Exp $
 */

@RunWith(SpecRunner.class)
@Feature("where&dochint��ѯ")
public class SR621160 extends DocdbTestTools {
    /** ��ʱ */
    private int timeOut    = 5000;
    List<Long>  docidsList = new ArrayList<Long>();

    @Before
    public void setUp() {
        stepInfo(" ��������");
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
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                docidsList.add(insertResult.getDocId());
                // Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Assert.isFalse(true, "ִ���쳣");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("where A and Bƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621160() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_IP, TEST_ID, GMT_TEST from TEST_DAILY indexwhere TEST_IP=? where GMT_TEST > ? and TEST_ID = ?";
        Object[] params = new Object[3];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(6);
        params[2] = "1";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("Ԥ�ڲ�ѯ����ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(2, resultList.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 2; i++) {
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڽ��");
            Assert.areEqual("1", resultList.get(i).get("TEST_ID"), "Ԥ�ڽ��");
        }
    }

    @Test
    @Subject("����indexwhere��where A or Bƥ��")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621161() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_IP, TEST_ID, GMT_TEST from TEST_DAILY where TEST_ID = ? or TEST_ID = ?";
        Object[] params = new Object[2];
        params[0] = "0";
        params[1] = "1";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("Ԥ�ڲ�ѯ����ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(10, resultList.size(), "Ԥ�ڲ�ѯ���");
        for (int i = 0; i < 10; i++) {
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("����indexwhere��dochint��ѯ")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621162() {
        stepInfo("ִ�в�ѯ�Լ�����ж�");
        String sql = "select TEST_ID, CIENT_IP, TEST_NAME, GMT_TEST from TEST_DAILY DOCHINT mile_doc_id=";
        Object[] params = null;

        try {
            for (int i = 0; i < 1; i++) {
                MileQueryResult queryResult = getApplationClientImpl().preQueryForList(
                    sql + docidsList.get(i), params, timeOut);
                List<Map<String, Object>> resultList = queryResult.getQueryResult();
                Assert.areEqual(String.valueOf(i % 2), resultList.get(0).get("TEST_ID"), "Ԥ�ڲ�ѯ���");
                Assert.areEqual(Long.valueOf(i), resultList.get(0).get("GMT_TEST"), "Ԥ�ڲ�ѯ���");
            }
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_DAILY indexwhere TEST_IP=?";
            String[] params = new String[1];
            params[0] = "127.0.0.1";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }
}
