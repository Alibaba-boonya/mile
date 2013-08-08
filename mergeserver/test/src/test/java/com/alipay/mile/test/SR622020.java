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
 * @version $Id: SR622020.java,v 0.1 2012-11-8 ����07:07:29 xiaoju.luo Exp $
 */
@RunWith(SpecRunner.class)
@Feature("һ�������µľۺϿ��ٲ�ѯ")
public class SR622020 extends LevdbTestTools {

    /**��ʱ*/
    private int timeOut = 5000;

    @Before
    public void setUp() {

        stepInfo("��������");
       // String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CREATE=?";
        String sql = "insert into TEST_VELOCITY TEST_ID=? TEST_NAME=? TEST_IP=? GMT_TEST=? ROWKEY=? GMT_CTEST=?";
        Object[] params = new Object[6];
        params[0] = "12345";
        params[1] = "milemac";
        params[2] = "127.0.0.3";
        params[4] = "rowkey";
        for (int i = 0; i < 10; i++) {
            MileInsertResult insertResult;
            params[3] = Long.valueOf(i);
            params[5] = Long.valueOf(i);
            try {
                insertResult = getApplationClientImpl().preInsert(sql, params, timeOut);
            } catch (Exception e) {
                Assert.isFalse(true, "����ʧ��");
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.isFalse(true, "ִ���쳣");
        }
    }

    @Test
    @Subject("�ۺϿ��ٺ��������־ۺϺ���������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622020() {
        stepInfo("ִ�в�ѯ");
        String sql = "select count(distinct TEST_ID) within (GMT_TEST > ?)  as a1, count(*) within(GMT_TEST > ?) as a2, sum(GMT_TEST) within(GMT_TEST>?) as a3 from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params = new Object[4];
        params[0] = Long.valueOf(1);
        params[1] = Long.valueOf(0);
        params[2] = Long.valueOf(0);
        params[3] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(Long.valueOf(1), queryResult.getQueryResult().get(0).get("a1"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("a2"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(45), queryResult.getQueryResult().get(0).get("a3"), "Ԥ�ڽ��");

    }

    //��������

    @Test
    @Subject("�ۺϿ��ٺ���������֧�ֵľۺϺ����Ҵ�����")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622021() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) within(GMT_TEST > ?) as A,squaresum(GMT_TEST) within(GMT_TEST>?) as B,stddev(GMT_TEST) within(GMT_TEST>=?) as C,avg(GMT_TEST) within(GMT_TEST>?) as D,sum(GMT_TEST) within(GMT_TEST>?)as E,min(GMT_TEST) within(GMT_TEST>?) as F, max(GMT_TEST) within(GMT_TEST>?) as G,count(GMT_TEST) within(GMT_TEST>?) as H, count(*) within(GMT_TEST>?) as I,count(distinct GMT_TEST) within(GMT_TEST>?) as J from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params = new Object[11];
        params[0] = Long.valueOf(1);
        params[1] = Long.valueOf(1);
        params[2] = Long.valueOf(0);
        params[3] = Long.valueOf(1);
        params[4] = Long.valueOf(1);
        params[5] = Long.valueOf(1);
        params[6] = Long.valueOf(1);
        params[7] = Long.valueOf(1);
        params[8] = Long.valueOf(1);
        params[9] = Long.valueOf(1);
        params[10] = "rowkey";

        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }
        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(5.25), queryResult.getQueryResult().get(0).get("A"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(284), queryResult.getQueryResult().get(0).get("B"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0)
            .get("C"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(5.5), queryResult.getQueryResult().get(0).get("D"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(44), queryResult.getQueryResult().get(0).get("E"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(2), queryResult.getQueryResult().get(0).get("F"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("G"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("H"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("I"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("J"), "Ԥ�ڽ��");
    }

    //��Ҫȷ���Ƿ�֧�ֲ�������
    @Test
    @Subject("�ۺϿ��ٺ���������֧�ֵľۺϺ����Ҳ�������")
    @Priority(PriorityLevel.HIGHEST)
    @Tester("xiaoju.luo")
    public void TC622022() {
        stepInfo("ִ�в�ѯ");
        String sql = "select variance(GMT_TEST) within(GMT_TEST > ?) ,squaresum(GMT_TEST) within(GMT_TEST>?) ,stddev(GMT_TEST) within(GMT_TEST>=?) ,avg(GMT_TEST) within(GMT_TEST>?) ,sum(GMT_TEST) within(GMT_TEST>?),min(GMT_TEST) within(GMT_TEST>?), max(GMT_TEST) within(GMT_TEST>?),count(GMT_TEST) within(GMT_TEST>?), count(*) within(GMT_TEST>?) ,count(distinct GMT_TEST) within(GMT_TEST>?) from TEST_VELOCITY indexwhere ROWKEY=?";
        Object[] params = new Object[11];
        params[0] = Long.valueOf(1);
        params[1] = Long.valueOf(1);
        params[2] = Long.valueOf(0);
        params[3] = Long.valueOf(1);
        params[4] = Long.valueOf(1);
        params[5] = Long.valueOf(1);
        params[6] = Long.valueOf(1);
        params[7] = Long.valueOf(1);
        params[8] = Long.valueOf(1);
        params[9] = Long.valueOf(1);
        params[10] = "rowkey";
        MileQueryResult queryResult = null;
        try {
            queryResult = getApplationClientImpl().preQueryForList(sql, params, timeOut);
        } catch (Exception e) {
            Logger.info("ִ���쳣" + e);
            Assert.isFalse(true, "��ѯִ���쳣");
        }

        stepInfo("��ѯ����ж�");
        Assert.areEqual(Double.valueOf(5.25), queryResult.getQueryResult().get(0).get(
            "VARIANCE (GMT_TEST)"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(284), queryResult.getQueryResult().get(0).get(
            "SQUARESUM (GMT_TEST)"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(2.8722813232690143), queryResult.getQueryResult().get(0)
            .get("STDDEV (GMT_TEST)"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(5.5), queryResult.getQueryResult().get(0).get(
            "AVG (GMT_TEST)"), "Ԥ�ڽ��");
        Assert.areEqual(Double.valueOf(44), queryResult.getQueryResult().get(0).get(
            "SUM (GMT_TEST)"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(2), queryResult.getQueryResult().get(0).get("MIN (GMT_TEST)"),
            "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(9), queryResult.getQueryResult().get(0).get("MAX (GMT_TEST)"),
            "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0)
            .get("COUNT (GMT_TEST)"), "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get("COUNT (*)"),
            "Ԥ�ڽ��");
        Assert.areEqual(Long.valueOf(8), queryResult.getQueryResult().get(0).get(
            "COUNT (DISTINCT GMT_TEST)"), "Ԥ�ڽ��");
    }

    @After
    public void tearDown() {
        try {
            stepInfo("��������");
            String sql = "delete from TEST_VELOCITY indexwhere ROWKEY=?";
            String[] params = new String[1];
            params[0] = "rowkey";
            getApplationClientImpl().preDelete(sql, params, timeOut);
        } catch (Exception e) {
            Assert.isFalse(true, "��������ʧ��");
        }
    }
}
