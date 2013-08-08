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
 * @version $Id: SR622150.java,v 0.1 2012-11-9 ����06:35:19 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("between��ѯ")
public class SR622150 extends LevdbTestTools {
    /** ��ʱ */
    private int timeOut    = 5000;

    List<Long>  docidsList = new ArrayList<Long>();

    @Before
    public void setUp() {
        stepInfo("ִ�в���");
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[6];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.1";
        params[4] = "rowkey";

        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = Long.valueOf(i);
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                //	Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Logger.info("docid: " + e);
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
    @Subject("between []����������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622150() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST between [?, ?] ";
        Object[] params = new Object[3];
        params[0] = "rowkey";
        params[1] = Long.valueOf(0);
        params[2] = Long.valueOf(3);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("�Բ�ѯ������ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(4, resultList.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 4; i++) {
            Assert.areEqual("12345", resultList.get(i).get("TEST_ID"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("milemac", resultList.get(i).get("TEST_NAME"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڲ�ѯ���");
            boolean b = (Long) resultList.get(i).get("GMT_TEST") >= 0;
            boolean c = (Long) resultList.get(i).get("GMT_TEST") <= 3;
            Assert.areEqual(true, b, "Ԥ�ڲ�ѯ���");
            Assert.areEqual(true, c, "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("between (]����������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622151() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST between (?, ?] ";
        Object[] params = new Object[3];
        params[0] = "rowkey";
        params[1] = Long.valueOf(0);
        params[2] = Long.valueOf(3);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("�Բ�ѯ������ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(3, resultList.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 3; i++) {
            Assert.areEqual("12345", resultList.get(i).get("TEST_ID"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("milemac", resultList.get(i).get("TEST_NAME"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڲ�ѯ���");
            boolean b = (Long) resultList.get(i).get("GMT_TEST") > 0;
            boolean c = (Long) resultList.get(i).get("GMT_TEST") <= 3;
            Assert.areEqual(true, b, "Ԥ�ڲ�ѯ���");
            Assert.areEqual(true, c, "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("between ()����������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622152() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST between (?, ?) ";
        Object[] params = new Object[3];
        params[0] = "rowkey";
        params[1] = Long.valueOf(0);
        params[2] = Long.valueOf(3);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("�Բ�ѯ������ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(2, resultList.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 2; i++) {
            Assert.areEqual("12345", resultList.get(i).get("TEST_ID"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("milemac", resultList.get(i).get("TEST_NAME"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڲ�ѯ���");
            boolean b = (Long) resultList.get(i).get("GMT_TEST") > 0;
            boolean c = (Long) resultList.get(i).get("GMT_TEST") < 3;
            Assert.areEqual(true, b, "Ԥ�ڲ�ѯ���");
            Assert.areEqual(true, c, "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("between [)����������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622153() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, TEST_NAME, TEST_IP, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? where GMT_TEST between [?, ?) ";
        Object[] params = new Object[3];
        params[0] = "rowkey";
        params[1] = Long.valueOf(0);
        params[2] = Long.valueOf(3);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("�Բ�ѯ������ж�");
        List<Map<String, Object>> resultList = queryResult.getQueryResult();
        Assert.areEqual(3, resultList.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 3; i++) {
            Assert.areEqual("12345", resultList.get(i).get("TEST_ID"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("milemac", resultList.get(i).get("TEST_NAME"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual("127.0.0.1", resultList.get(i).get("TEST_IP"), "Ԥ�ڲ�ѯ���");
            boolean b = (Long) resultList.get(i).get("GMT_TEST") >= 0;
            boolean c = (Long) resultList.get(i).get("GMT_TEST") < 3;
            Assert.areEqual(true, b, "Ԥ�ڲ�ѯ���");
            Assert.areEqual(true, c, "Ԥ�ڲ�ѯ���");
        }
    }

    @After
    public void tearDown() {
        try {
            stepInfo("ɾ������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }
}
