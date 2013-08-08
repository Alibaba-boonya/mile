/**
 * 
 */
package com.alipay.mile.test;

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
 * @version $Id: SR622140.java,v 0.1 2012-11-9 ����06:25:11 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("һ�������µľۺϲ�ѯ+order by")
public class SR622140 extends LevdbTestTools {

    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("ִ�в���");
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
                Logger.info("�쳣Ϊ" + e);
                Assert.isFalse(true, "�����쳣");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Logger.info("�쳣Ϊ" + e);
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("order by asc+limit��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622140() {
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST limit 1000";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(100, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 100; i++) {
            Map<String, Object> record = queryResult.getQueryResult().get(i);
            Assert.areEqual(Long.valueOf(i), record.get("GMT_TEST"), "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("order by dessc+limit��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621141() {
        stepInfo("ִ�в�ѯ");
        String sql = "select GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST desc limit 1000";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(100, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 100; i++) {
            Map<String, Object> record = queryResult.getQueryResult().get(i);
            Assert.areEqual(Long.valueOf(99 - i), record.get("GMT_TEST"), "Ԥ�ڽ������С");
        }
    }

    @Test
    @Subject("distinct+order by asc��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622142() {
        stepInfo("ִ�в�ѯ");
        String sql = "select distinct GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(100, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 10; i++) {
            Map<String, Object> record = queryResult.getQueryResult().get(i);
            Assert.areEqual(Long.valueOf(i), record.get("GMT_TEST"), "Ԥ�ڽ��");
        }
    }

    @Test
    @Subject("distinct,����+order by asc��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622143() {
        stepInfo("ִ�в�ѯ");
        String sql = "select distinct TEST_ID, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {

            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(100, queryResult.getQueryResult().size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 10; i++) {
            Map<String, Object> record = queryResult.getQueryResult().get(i);
            Assert.areEqual(Long.valueOf(i), record.get("GMT_TEST"), "Ԥ�ڽ������С");
            Assert.areEqual(String.valueOf(i), record.get("TEST_ID"), "Ԥ�ڽ������С");
        }
    }

    @Test
    @Subject("sum as ����+group by+order by��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622144() {
        stepInfo("ִ�в�ѯ");
        String sql = "select sum(GMT_TEST) as v1 from TEST_VELOCITY indexwhere ROWKEY=? group by TEST_ID order by v1";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        List<Map<String, Object>> result = queryResult.getQueryResult();
        Assert.areEqual(10, result.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 10; i++) {
            Map<String, Object> record = result.get(i);
            Assert.areEqual(1, record.size(), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(Double.valueOf(450 + 10 * i), record.get("v1"), "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("order by+limit��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622145() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST limit 10";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        List<Map<String, Object>> result = queryResult.getQueryResult();
        Assert.areEqual(10, result.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 10; i++) {
            Map<String, Object> record = result.get(i);
            Assert.areEqual(Long.valueOf(i), record.get("GMT_TEST"), "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("order by+limit+offset��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622146() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST limit 100 offset 10";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("Ԥ�ڽ���ж�");
        List<Map<String, Object>> result = queryResult.getQueryResult();
        Assert.areEqual(90, result.size(), "Ԥ�ڽ������С");
        for (int i = 10; i < 100; i++) {
            Map<String, Object> record = result.get(i - 10);
            Assert.areEqual(Long.valueOf(i), record.get("GMT_TEST"), "Ԥ�ڲ�ѯ���");
        }
    }

    @Test
    @Subject("order by+limit A+offsetA,�����Ϊ0��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622147() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, GMT_TEST from TEST_VELOCITY indexwhere ROWKEY=? order by GMT_TEST limit 100 offset 100";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);

        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
        stepInfo("Ԥ�ڽ���ж�");
        List<Map<String, Object>> result = queryResult.getQueryResult();
        Assert.areEqual(0, result.size(), "Ԥ�ڽ������С");
    }

    @Test
    @Subject("order by A,B��������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622148() {
        stepInfo("ִ�в�ѯ");
        String sql = "select TEST_ID, GMT_TEST, VALUE from TEST_VELOCITY indexwhere ROWKEY=? order by VALUE, GMT_TEST";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = applationClientImpl.preQueryForList(sql, params, timeOut);

        } catch (Exception e) {

            Assert.isFalse(true, "ִ���쳣");
        }

        stepInfo("Ԥ�ڽ���ж�");
        List<Map<String, Object>> result = queryResult.getQueryResult();
        Assert.areEqual(100, result.size(), "Ԥ�ڽ������С");
        for (int i = 0; i < 100; i++) {
            Map<String, Object> record = result.get(i);
            Assert.areEqual(Long.valueOf(i), record.get("GMT_TEST"), "Ԥ�ڲ�ѯ���");
            Assert.areEqual(null, record.get("VALUE"), "Ԥ�ڲ�ѯ���");
        }
    }

    @After
    public void tearDown() {

        stepInfo("ִ��ɾ������");
        String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        try {
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }
}
