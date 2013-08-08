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
 * @version $Id: SR622100.java,v 0.1 2012-11-9 ����06:09:11 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("�ۺϺ�����group by��limit")
public class SR622100 extends LevdbTestTools {
    int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[6];

        for (int i = 0; i < 100; i++) {
            MileInsertResult insertResult;
            params[0] = String.valueOf(i / 10);
            params[1] = String.valueOf(i / 10);
            params[2] = "127.0.0.1";
            params[3] = Long.valueOf(i);
            params[4] = "rowkey";
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
                //	Logger.info("docid: " + insertResult.getDocId());
            } catch (Exception e) {
                Logger.info("�쳣Ϊ��" + e);
                Assert.isFalse(true, "����ʧ��");
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "����ʧ��");
        }
    }

    @Test
    @Subject("�ۺϺ�����count,max,min+as����+������+group by+limit��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622100() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct GMT_TEST) as a, max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, count(distinct TEST_IP) as b, TEST_NAME from TEST_VELOCITY indexwhere ROWKEY=? group by TEST_ID, TEST_NAME limit 1000";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.error("�쳣" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(10, queryResult.getQueryResult().size(), "��ѯ�������С");
        for (Map<String, Object> record : queryResult.getQueryResult()) {
            int i = Integer.parseInt((String) record.get("TEST_NAME"));
            Assert.areEqual(Long.valueOf(i * 10 + 9), record.get("v1"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(i * 10), record.get("v2"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Double.valueOf(i * 100 + 45), record.get("v3"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(10), record.get("v4"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(10), record.get("a"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(1), record.get("b"), "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("�ۺϺ�����count,count(distinct),count(),max,min+group by ������+limit��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622101() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct GMT_TEST) as a, max(GMT_TEST) as v1, min(GMT_TEST) as v2, sum(GMT_TEST) as v3, count(GMT_TEST) as v4, count(distinct TEST_IP) as b from TEST_VELOCITY indexwhere ROWKEY=? group by TEST_ID limit 1000";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.error("�쳣Ϊ��" + e);
            Assert.isFalse(true, "����ʧ��");
        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(10, queryResult.getQueryResult().size(), "Ԥ�ڲ�ѯ�������С");
        for (Map<String, Object> record : queryResult.getQueryResult()) {
            int i = (Integer.parseInt(record.get("v1").toString()) - 9) / 10;
            Assert.areEqual(Long.valueOf(i * 10 + 9), record.get("v1"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(i * 10), record.get("v2"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Double.valueOf(i * 100 + 45), record.get("v3"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(10), record.get("v4"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(10), record.get("a"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Long.valueOf(1), record.get("b"), "Ԥ�ڲ�ѯ���");
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
            Assert.isFalse(true, "����ʧ��");
        }
    }
}
