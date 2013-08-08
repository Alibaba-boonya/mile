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
 * @version $Id: SR622090.java,v 0.1 2012-11-9 ����06:01:47 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("group by����")
public class SR622090 extends LevdbTestTools {

    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[6];

        for (int i = 0; i < 100; i++) {
            MileInsertResult insertResult;
            params[0] = String.valueOf(i % 10);
            params[1] = String.valueOf(i);
            params[2] = "127.0.0.1";
            params[3] = Long.valueOf(i);
            params[4] = "rowkey";
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                //	Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Logger.info("�쳣Ϊ��" + e);
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
    public void TC622090() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, TEST_ID from TEST_VELOCITY indexwhere ROWKEY=? group by TEST_ID having (max(GMT_TEST) >= ? and min(GMT_TEST) <= ?) or (max(GMT_TEST) <= ? and min(GMT_TEST) >= ?)";
        Object[] params = new Object[5];
        params[0] = "rowkey";
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
    public void TC622091() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, TEST_ID from TEST_VELOCITY indexwhere ROWKEY=? group by TEST_ID";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
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
    public void TC622092() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, TEST_ID from TEST_VELOCITY indexwhere ROWKEY=? group by TEST_ID having max(GMT_TEST) >= ?";
        Object[] params = new Object[2];
        params[0] = "rowkey";
        params[1] = Long.valueOf(95);
        MileQueryResult queryResult = null;
        try {
            queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);
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
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }
}
