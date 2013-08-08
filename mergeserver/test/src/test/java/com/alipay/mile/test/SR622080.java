/**
 * 
 */
package com.alipay.mile.test;

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
 * @version $Id: SR622080.java,v 0.1 2012-11-9 ����05:42:33 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("�ۺϺ���")
public class SR622080 extends LevdbTestTools {
    private int timeOut = 5000;

    @Before
    public void setUp() {
        stepInfo("��������");
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
                Logger.info("�쳣Ϊ��" + e);
                Assert.isFalse(true, "�����쳣ʧ��");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ��ʧ��");
        }
    }

    @Test
    @Subject("�ۺϺ�����count(*)&count(distinct)Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622080() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct GMT_TEST) as a, count(distinct TEST_NAME) as b from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "��ѯִ��ʧ��");
        }
        stepInfo("�Բ�ѯ�����Ԥ�ڵ��ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "Ԥ�ڽ����С");
     
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("a"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(1), queryResult.getQueryResult().get(0).get("b"), "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ۺϺ�����count*��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622081() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(*) from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
            Logger.info("�����Ϊ��" + queryResult);
        } catch (Exception e) {
            Assert.isFalse(true, "ִ���쳣");
        }
        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("COUNT (*)"),
            "Ԥ�ڲ�ѯ���");

    }

    @Test
    @Subject("�ۺϺ�����max��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622082() {
        stepInfo("ִ�в�ѯ");
        String sql = "select max(GMT_TEST) from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        Logger.info("��ѯ���Ϊ" + queryResult);
        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("MAX (GMT_TEST)"),
            "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ۺϺ�����min��ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622083() {
        stepInfo("ִ�в�ѯ");
        String sql = "select MIN(GMT_TEST) from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Long.valueOf(0), queryResult.getQueryResult().get(0).get("MIN (GMT_TEST)"),
            "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ۺϺ�����2��sum�ظ���ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC621084() {
        stepInfo("ִ�в�ѯ");
        String sql = "select sum(GMT_TEST), sum(GMT_TEST) from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get(
            "SUM (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ۺϺ�����1��sum�ظ���ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622085() {
        String sql = "select sum(GMT_TEST) from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("�쳣��" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get(
            "SUM (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ۺϺ�����max,min,sum��count��ϲ�ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622086() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct GMT_TEST) as a, max(GMT_TEST) as c, min(GMT_TEST) as d, sum(GMT_TEST) as e, count(*) as f, count(distinct TEST_NAME) as b from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
            Logger.info("��ѯ���Ϊ" + queryResult);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("a"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(1), queryResult.getQueryResult().get(0).get("b"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("c"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(0), queryResult.getQueryResult().get(0).get("d"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get("e"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get("f"), "Ԥ�ڲ�ѯ���");
    }

    @Test
    @Subject("�ۺϺ�����sum,count,min,max,avg,variance,stddev�Ⱥ�����ϲ�ѯ��Ӣ�ĳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622087() {
        stepInfo("ִ�в�ѯ");
        String sql = "select sum(GMT_TEST), count(GMT_TEST), min(GMT_TEST), max(GMT_TEST), avg(GMT_TEST), variance(GMT_TEST), stddev(GMT_TEST), squaresum(GMT_TEST) from TEST_VELOCITY indexwhere ROWKEY=?";
        String[] params = new String[1];
        params[0] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("�Բ�ѯ�����Ԥ���ж�");
        Assert.areEqual(1, queryResult.getQueryResult().size(), "�������СԤ��");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get(
            "SUM (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("MAX (GMT_TEST)"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(0), queryResult.getQueryResult().get(0).get("MIN (GMT_TEST)"),
            "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Long.valueOf(10), queryResult.getQueryResult().get(0).get(
            "COUNT (GMT_TEST)"), "Ԥ�ڲ�ѯ���");

        Assert.areEqual(Double.valueOf(4.5), queryResult.getQueryResult().get(0).get(
            "AVG (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0)
            .get("STDDEV (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Double.valueOf(8.25), queryResult.getQueryResult().get(0).get(
            "VARIANCE (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
        Assert.areEqual(Double.valueOf(285), queryResult.getQueryResult().get(0).get(
            "SQUARESUM (GMT_TEST)"), "Ԥ�ڲ�ѯ���");
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
            Assert.isFalse(true, "�������ʧ��");
        }
    }
}
