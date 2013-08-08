/**
 * 
 */
package com.alipay.mile.test;

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
 * @version $Id: SR621090.java,v 0.1 2012-11-6 ����02:43:42 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("group by����")
public class SR621090 extends DocdbTestTools {
    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_DAILY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=?";
        Object[] params = new Object[4];

        for (int i = 0; i < 100; i++) {
            MileInsertResult insertResult;
            params[0] = String.valueOf(i % 10);
            params[1] = String.valueOf(i);
            params[2] = "127.0.0.1";
            params[3] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                // Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Assert.isFalse(true, "�����쳣");
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("�ۺϺ�����sum,min,max+group by+having��ϲ�ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621090() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, TEST_ID from TEST_DAILY indexwhere TEST_IP=? group by TEST_ID having (max(GMT_TEST) >= ? and min(GMT_TEST) <= ?) or (max(GMT_TEST) <= ? and min(GMT_TEST) >= ?)";
        Object[] params = new Object[5];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(95);
        params[2] = Long.valueOf(7);
        params[3] = Long.valueOf(95);
        params[4] = Long.valueOf(3);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.error("����" + e);
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(5, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (Map<String, Object> record : queryResult.getQueryResult()) {
            Assert.areEqual(5, record.size(), "Ԥ�ڽ��");
            int i = Integer.parseInt((String) record.get("TEST_ID"));
            Assert.isTrue(i >= 3 && i <= 7, "Ԥ�ڽ��");
            // assertTrue(i >= 3 && i <= 7);
            Assert.areEqual(Long.valueOf(90 + i), record.get("v1"), "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(i), record.get("v2"), "Ԥ�ڽ��");
            Assert.areEqual(Double.valueOf(450 + i * 10), record.get("v3"), "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(10), record.get("v4"), "Ԥ�ڽ��");
        }

    }

    @Test
    @Subject("�ۺϺ�����SimpleGroupby��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621091() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, TEST_ID from TEST_DAILY indexwhere TEST_IP=? group by TEST_ID";
        String[] params = new String[1];
        params[0] = "127.0.0.1";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");

        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(10, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (Map<String, Object> record : queryResult.getQueryResult()) {
            Assert.areEqual(5, record.size(), "Ԥ�ڽ������С");
            int i = Integer.parseInt((String) record.get("TEST_ID"));
            Assert.areEqual(Long.valueOf(90 + i), record.get("v1"), "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(i), record.get("v2"), "Ԥ�ڽ��");
            Assert.areEqual(Double.valueOf(450 + i * 10), record.get("v3"), "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(10), record.get("v4"), "Ԥ�ڽ��");
        }
    }

    @Test
    @Subject("�ۺϺ�����SimpleHavingGroupby��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621092() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, TEST_ID from TEST_DAILY indexwhere TEST_IP=? group by TEST_ID having max(GMT_TEST) >= ?";
        Object[] params = new Object[2];
        params[0] = "127.0.0.1";
        params[1] = Long.valueOf(95);
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.error("����Ϊ��" + e);
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(5, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (Map<String, Object> record : queryResult.getQueryResult()) {
            Assert.areEqual(5, record.size(), "Ԥ�ڽ������С");
            int i = Integer.parseInt((String) record.get("TEST_ID"));
            Assert.isTrue(i >= 5 && i < 10, "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(90 + i), record.get("v1"), "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(i), record.get("v2"), "Ԥ�ڽ��");
            Assert.areEqual(Double.valueOf(450 + i * 10), record.get("v3"), "Ԥ�ڽ��");
            Assert.areEqual(Long.valueOf(10), record.get("v4"), "Ԥ�ڽ��");
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
